package piuk.blockchain.android.ui.buysell.payment

import info.blockchain.api.data.UnspentOutputs
import info.blockchain.wallet.api.data.FeeOptions
import info.blockchain.wallet.payload.data.Account
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.subjects.PublishSubject
import piuk.blockchain.android.R
import piuk.blockchain.android.data.cache.DynamicFeeCache
import piuk.blockchain.android.data.datamanagers.FeeDataManager
import piuk.blockchain.android.data.payments.SendDataManager
import piuk.blockchain.android.ui.buysell.payment.models.OrderType
import piuk.blockchain.android.util.StringUtils
import piuk.blockchain.android.util.extensions.addToCompositeDisposable
import piuk.blockchain.androidbuysell.datamanagers.CoinifyDataManager
import piuk.blockchain.androidbuysell.models.coinify.KycResponse
import piuk.blockchain.androidbuysell.models.coinify.LimitInAmounts
import piuk.blockchain.androidbuysell.models.coinify.Medium
import piuk.blockchain.androidbuysell.models.coinify.PaymentMethod
import piuk.blockchain.androidbuysell.models.coinify.Quote
import piuk.blockchain.androidbuysell.models.coinify.Trader
import piuk.blockchain.androidbuysell.services.ExchangeService
import piuk.blockchain.androidcore.data.currency.BTCDenomination
import piuk.blockchain.androidcore.data.currency.CurrencyFormatManager
import piuk.blockchain.androidcore.data.exchangerate.ExchangeRateDataManager
import piuk.blockchain.androidcore.data.payload.PayloadDataManager
import piuk.blockchain.androidcore.utils.extensions.applySchedulers
import piuk.blockchain.androidcoreui.ui.base.BasePresenter
import piuk.blockchain.androidcoreui.ui.customviews.ToastCustom
import timber.log.Timber
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.ParseException
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.absoluteValue
import kotlin.properties.Delegates

class BuySellBuildOrderPresenter @Inject constructor(
        private val coinifyDataManager: CoinifyDataManager,
        private val sendDataManager: SendDataManager,
        private val payloadDataManager: PayloadDataManager,
        private val exchangeService: ExchangeService,
        private val currencyFormatManager: CurrencyFormatManager,
        private val feeDataManager: FeeDataManager,
        private val exchangeRateDataManager: ExchangeRateDataManager,
        private val dynamicFeeCache: DynamicFeeCache,
        private val stringUtils: StringUtils
) : BasePresenter<BuySellBuildOrderView>() {

    val receiveSubject: PublishSubject<String> = PublishSubject.create<String>()
    val sendSubject: PublishSubject<String> = PublishSubject.create<String>()
    var account by Delegates.observable(payloadDataManager.defaultAccount) { _, old, new ->
        if (old != new) {
            view.updateAccountSelector(new.label)
            loadMax(new)
        }
    }

    var selectedCurrency: String? by Delegates.observable<String?>(null) { _, old, new ->
        if (old != new) initialiseUi(); subscribeToSubjects()
    }

    private var latestQuote: Quote? = null
    private var latestLoadedLimits: LimitInAmounts? = null
    private var feeOptions: FeeOptions? = null
    // The user's daily cardLimit in their default fiat denomination
    private var cardLimitMax: Double = 0.0
    // The bank buy limit in the user's default fiat denomination
    private var bankLimitMax: Double = 0.0
    // The bank sell limit in the user's default fiat denomination
    private var bankSellLimitMax: Double = 0.0
    private var minimumInAmount: Double = 0.0
    // The user's max spendable bitcoin
    private var maxBitcoinAmount: BigDecimal = BigDecimal.ZERO
    private var defaultCurrency: String = "usd"
    private var initialLoad = true

    private val emptyQuote
        get() = Quote(null, selectedCurrency!!, "BTC", 0.0, 0.0, "", "")

    private val tokenSingle: Single<String>
        get() = exchangeService.getExchangeMetaData()
                .addToCompositeDisposable(this)
                .applySchedulers()
                .singleOrError()
                .doOnError { view.onFatalError() }
                .map { it.coinify!!.token }

    private val inMediumSingle: Single<Medium>
        get() = when (view.orderType) {
            OrderType.Sell -> Single.just(Medium.Blockchain)
            OrderType.Buy, OrderType.BuyCard -> tokenSingle
                    .flatMap { coinifyDataManager.getKycReviews(it) }
                    // Here we assume Bank payment as it has higher limits
                    .map { if (it.hasPendingKyc()) Medium.Card else Medium.Bank }
        }

    // TODO: 2) Cache buy limits for chosen payment type, both max and min
    // TODO: 4) Check amounts against limits, notify UI if min < x > max

    override fun onViewReady() {
        // Display Accounts selector if necessary
        if (payloadDataManager.accounts.size > 1) {
            view.displayAccountSelector(account.label)
        }

        initialiseUi()
        subscribeToSubjects()
        loadMax(account)
    }

    internal fun onMaxClicked() {
        when (view.orderType) {
        // TODO: Sell Limit
            OrderType.Sell -> view.updateReceiveAmount(cardLimitMax.toString())
        // TODO: Bank limit?
            OrderType.Buy -> view.updateSendAmount(bankLimitMax.toString())
            OrderType.BuyCard -> view.updateSendAmount(cardLimitMax.toString())
        }
    }

    private fun subscribeToSubjects() {
        sendSubject.applyDefaults()
                .flatMapSingle { amount ->
                    // TODO: If amount > maxBitcoinAmount, fail
                    // TODO: Needs converting to/from
                    tokenSingle.flatMap {
                        coinifyDataManager.getQuote(
                                it,
                                amount.unaryMinus(),
                                selectedCurrency!!,
                                "BTC"
                        ).doOnSuccess { latestQuote = it }
                                .onErrorReturn { emptyQuote }
                                .doAfterSuccess { view.showQuoteInProgress(false) }
                    }
                }
                .doOnNext { updateReceiveAmount(it.quoteAmount.absoluteValue) }
                .doOnNext { updateSendAmount(it.baseAmount.absoluteValue) }
                .doOnNext { compareToLimits(it) }
                .subscribeBy(onError = { setUnknownErrorState(it) })

        receiveSubject.applyDefaults()
                .flatMapSingle { amount ->
                    // TODO: If amount > maxBitcoinAmount, fail
                    // TODO: Needs converting to/from
                    tokenSingle.flatMap {
                        coinifyDataManager.getQuote(
                                it,
                                amount,
                                "BTC",
                                selectedCurrency!!
                        ).doOnSuccess { latestQuote = it }
                                .onErrorReturn { emptyQuote }
                                .doAfterSuccess { view.showQuoteInProgress(false) }
                    }
                }
                .doOnNext { updateSendAmount(it.quoteAmount.absoluteValue) }
                .doOnNext { updateReceiveAmount(it.baseAmount.absoluteValue) }
                .doOnNext { compareToLimits(it) }
                .subscribeBy(onError = { setUnknownErrorState(it) })
    }

    private fun compareToLimits(quote: Quote) {
        val amountToReceive = when (view.orderType) {
            OrderType.Buy, OrderType.BuyCard -> if (quote.quoteAmount >= 0) quote.quoteAmount else quote.baseAmount
            OrderType.Sell -> if (quote.baseAmount >= 0) quote.baseAmount else quote.quoteAmount
        }.absoluteValue.toBigDecimal()

        val amountToSend = when (view.orderType) {
            OrderType.Buy, OrderType.BuyCard -> if (quote.baseAmount >= 0) quote.quoteAmount else quote.baseAmount
            OrderType.Sell -> if (quote.quoteAmount >= 0) quote.baseAmount else quote.quoteAmount
        }.absoluteValue.toBigDecimal()

        Timber.d("maxBitcoinAmount = $maxBitcoinAmount")
        Timber.d("minimumInAmount = $minimumInAmount")
        Timber.d("bankLimitMax = $bankLimitMax")
        Timber.d("cardLimitMax = $cardLimitMax")
        Timber.d("bankSellLimitMax = $bankSellLimitMax")
        Timber.d("amountToReceive = $amountToReceive")
        Timber.d("amountToSend = $amountToSend")
        val orderType = view.orderType

        // Attempting to sell more bitcoin than you have
        if (orderType == OrderType.Sell && amountToReceive > maxBitcoinAmount) {
            view.renderLimitStatus(
                    LimitStatus.ErrorData(
                            R.string.buy_sell_not_enough_bitcoin,
                            maxBitcoinAmount.toPlainString()
                    )
            )

            // Attempting to buy less than is allowed
        } else if ((orderType == OrderType.Buy || orderType == OrderType.BuyCard)
            && amountToSend < minimumInAmount.toBigDecimal()
        ) {
            view.renderLimitStatus(
                    LimitStatus.ErrorData(
                            R.string.buy_sell_amount_too_low,
                            "$minimumInAmount $selectedCurrency"
                    )
            )
            // Attempting to buy more than allowed via Bank
        } else if (orderType == OrderType.Buy && amountToSend > bankLimitMax.toBigDecimal()) {
            view.renderLimitStatus(
                    LimitStatus.ErrorData(
                            R.string.buy_sell_remaining_buy_limit,
                            "$bankLimitMax $selectedCurrency"
                    )
            )
            // Attempting to buy more than allowed via Card
        } else if (orderType == OrderType.BuyCard && amountToSend > cardLimitMax.toBigDecimal()) {
            view.renderLimitStatus(
                    LimitStatus.ErrorData(
                            R.string.buy_sell_remaining_buy_limit,
                            "$cardLimitMax $selectedCurrency"
                    )
            )
            // Attempting to sell more than allowed
        } else if (orderType == OrderType.Sell && amountToReceive > bankSellLimitMax.toBigDecimal()) {
            view.renderLimitStatus(
                    LimitStatus.ErrorData(
                            R.string.buy_sell_remaining_sell_limit,
                            "$bankSellLimitMax $selectedCurrency"
                    )
            )
            // All good, reload previously stated limits
        } else {
            loadLimits(latestLoadedLimits!!)
        }

    }

    private fun loadMax(account: Account) {
        fetchFeesObservable()
                .flatMap { getBtcMaxObservable(account) }
                .subscribeBy(
                        onNext = { maxBitcoinAmount = it },
                        onError = {
                            Timber.e(it)
                            view.showToast(
                                    R.string.buy_sell_error_fetching_limit,
                                    ToastCustom.TYPE_ERROR
                            )
                        }
                )
    }

    private fun initialiseUi() {
        // Get quote for value of 1 BTC for UI using default currency
        tokenSingle
                .doOnSubscribe { view.renderSpinnerStatus(SpinnerStatus.Loading) }
                .flatMapObservable { token ->
                    Observable.zip(
                            coinifyDataManager.getTrader(token)
                                    .doOnSuccess {
                                        cardLimitMax = it.level?.limits?.card?.inX?.daily ?: 0.0
                                    }
                                    .toObservable(),
                            inMediumSingle.toObservable(),
                            BiFunction<Trader, Medium, Pair<Trader, Medium>> { trader, inMedium ->
                                return@BiFunction trader to inMedium
                            }
                    ).flatMap { (trader, inMedium) ->
                        // TODO: Minimum sell plus fee (for sell only)
                        // This requires trader info + bitcoin limits (for sell only)
                        // Web currently display the limits via quote instead..?
                        getExchangeRate(token, -1.0, trader.defaultCurrency)
                                .toObservable()
                                .flatMap { getPaymentMethods(token, inMedium).toObservable() }
                                .doOnNext { defaultCurrency = trader.defaultCurrency }
                                .doOnNext {
                                    if (initialLoad) {
                                        selectCurrencies(it, inMedium, trader.defaultCurrency)
                                        minimumInAmount = if (view.orderType == OrderType.Sell) {
                                            it.minimumInAmounts.getLimitsForCurrency("btc")
                                        } else {
                                            it.minimumInAmounts.getLimitsForCurrency(trader.defaultCurrency)
                                        }
                                        initialLoad = false
                                    }
                                }
                                .doOnNext { loadMaxLimits(it) }
                    }
                }
                .subscribeBy(
                        onError = {
                            Timber.e(it)
                            view.onFatalError()
                        }
                )
    }

    private fun updateReceiveAmount(quoteAmount: Double) {
        val formatted = currencyFormatManager
                .getFormattedBchValue(BigDecimal.valueOf(quoteAmount), BTCDenomination.BTC)
        view.updateReceiveAmount(formatted)
    }

    private fun updateSendAmount(quoteAmount: Double) {
        val formatted = currencyFormatManager
                .getFiatFormat(selectedCurrency!!).format(quoteAmount)
        view.updateSendAmount(formatted)
    }

    private fun setUnknownErrorState(throwable: Throwable) {
        Timber.e(throwable)
        view.clearEditTexts()
        view.setButtonEnabled(false)
        view.showToast(R.string.buy_sell_error_fetching_quote, ToastCustom.TYPE_ERROR)
    }

    private fun getPaymentMethods(token: String, inMedium: Medium): Single<PaymentMethod> =
            coinifyDataManager.getPaymentMethods(token)
                    .filter { it.inMedium == inMedium }
                    .firstOrError()

    private fun selectCurrencies(
            paymentMethod: PaymentMethod,
            inMedium: Medium,
            userCurrency: String
    ) {
        val currencies = when (inMedium) {
            Medium.Blockchain -> paymentMethod.outCurrencies.toMutableList() // Sell
            else -> paymentMethod.inCurrencies.toMutableList() // Buy
        }

        selectedCurrency = if (currencies.contains(userCurrency)) {
            val index = currencies.indexOf(userCurrency)
            currencies.removeAt(index)
            currencies.add(0, userCurrency)
            userCurrency
        } else {
            currencies[0]
        }

        view.renderSpinnerStatus(SpinnerStatus.Data(currencies))
    }

    private fun loadMaxLimits(paymentMethod: PaymentMethod) {
        loadLimits(paymentMethod.limitInAmounts)
    }

    private fun getExchangeRate(currencyCode: String): BigDecimal {
        val price = exchangeRateDataManager.getLastBtcPrice(currencyCode)
        return BigDecimal.valueOf(price)
    }

    private fun loadLimits(limits: LimitInAmounts) {
        latestLoadedLimits = limits
        val limitAmount = when {
            view.orderType == OrderType.Sell -> "${limits.btc} BTC"
            view.orderType == OrderType.BuyCard -> getLocalisedCardLimit()
            selectedCurrency == "GBP" -> "${limits.gbp} $selectedCurrency"
            selectedCurrency == "DKK" -> "${limits.dkk} $selectedCurrency"
            selectedCurrency == "EUR" -> "${limits.eur} $selectedCurrency"
            else -> "${limits.usd} $selectedCurrency"
        }

        val descriptionString = when (view.orderType) {
            OrderType.Buy, OrderType.BuyCard -> R.string.buy_sell_remaining_buy_limit
            OrderType.Sell -> R.string.buy_sell_remaining_sell_limit
        }

        view.renderLimitStatus(LimitStatus.Data(descriptionString, limitAmount))
    }

    private fun getLocalisedCardLimit(): String {
        val exchangeRateSelected = getExchangeRate(selectedCurrency!!)
        val exchangeRateDefault = getExchangeRate(defaultCurrency)
        val rate = exchangeRateSelected.div(exchangeRateDefault)
        val limit = rate.multiply(cardLimitMax.toBigDecimal()).setScale(2, RoundingMode.DOWN)

        return "$limit $selectedCurrency"
    }

    //region Observables
    private fun getExchangeRate(token: String, amount: Double, currency: String): Single<Quote> =
            coinifyDataManager.getQuote(token, amount, "BTC", currency)
                    .doOnSuccess {
                        val valueWithSymbol =
                                currencyFormatManager.getFormattedFiatValueWithSymbol(
                                        it.quoteAmount,
                                        it.quoteCurrency,
                                        view.locale
                                )

                        view.renderExchangeRate(ExchangeRateStatus.Data("@ $valueWithSymbol"))

                    }
                    .doOnError { view.renderExchangeRate(ExchangeRateStatus.Failed) }

    private fun getBtcMaxObservable(account: Account): Observable<BigDecimal> =
            getUnspentApiResponseBtc(account.xpub)
                    .addToCompositeDisposable(this)
                    .map { unspentOutputs ->
                        val sweepBundle = sendDataManager.getMaximumAvailable(
                                unspentOutputs,
                                BigInteger.valueOf(feeOptions!!.priorityFee * 1000)
                        )
                        val sweepableAmount =
                                BigDecimal(sweepBundle.left).divide(BigDecimal.valueOf(1e8))
                        return@map sweepableAmount to BigDecimal(sweepBundle.right).divide(
                                BigDecimal.valueOf(1e8)
                        )
                    }
                    .flatMap { Observable.just(it.first) }
                    .onErrorReturn { BigDecimal.ZERO }

    private fun fetchFeesObservable(): Observable<FeeOptions> = feeDataManager.btcFeeOptions
            .doOnSubscribe { feeOptions = dynamicFeeCache.btcFeeOptions!! }
            .doOnNext { dynamicFeeCache.btcFeeOptions = it }

    private fun getUnspentApiResponseBtc(address: String): Observable<UnspentOutputs> {
        return if (payloadDataManager.getAddressBalance(address).toLong() > 0) {
            sendDataManager.getUnspentOutputs(address)
        } else {
            Observable.error(Throwable("No funds - skipping call to unspent API"))
        }
    }

    private fun PublishSubject<String>.applyDefaults(): Observable<Double> = this.doOnNext {
        view.setButtonEnabled(false)
        view.showQuoteInProgress(true)
    }.debounce(1000, TimeUnit.MILLISECONDS)
            // Here we kill any quotes in flight already, as they take up to ten seconds to fulfill
            .doOnNext { compositeDisposable.clear() }
            // Strip out localised information for predictable formatting
            .map { it.sanitise().parse(view.locale) }
            // Logging
            .doOnError(Timber::wtf)
            // Return zero if empty or some other error
            .onErrorReturn { BigDecimal.ZERO }
            // Scheduling for UI updates if necessary
            .observeOn(AndroidSchedulers.mainThread())
            // If zero, clear all EditTexts and reset UI state
            .doOnNext {
                if (it <= BigDecimal.ZERO) {
                    view.clearEditTexts()
                    view.setButtonEnabled(false)
                    view.showQuoteInProgress(false)
                }
            }
            // Don't pass zero events to the API as they're invalid
            .filter { it > BigDecimal.ZERO }
            // To double, as API requires it
            .map { it.toDouble() }
            // Prevents focus issues
            .distinctUntilChanged()
    //endregion

    //region Extension Functions
    private fun String.sanitise() = if (isNotEmpty()) this else "0"

    @Throws(ParseException::class)
    private fun String.parse(locale: Locale): BigDecimal {
        val format = NumberFormat.getNumberInstance(locale)
        if (format is DecimalFormat) {
            format.isParseBigDecimal = true
        }
        return format.parse(this.replace("[^\\d.,]".toRegex(), "")) as BigDecimal
    }

    private fun List<KycResponse>.hasPendingKyc(): Boolean = this.any { it.state.isProcessing() }
    //endregion

    sealed class ExchangeRateStatus {

        object Loading : ExchangeRateStatus()
        data class Data(val formattedQuote: String) : ExchangeRateStatus()
        object Failed : ExchangeRateStatus()

    }

    sealed class SpinnerStatus {

        object Loading : SpinnerStatus()
        data class Data(val currencies: List<String>) : SpinnerStatus()
        object Failure : SpinnerStatus()

    }

    sealed class LimitStatus {
        object Loading : LimitStatus()
        data class Data(val textResourceId: Int, val limit: String) : LimitStatus()
        data class ErrorData(val textResourceId: Int, val limit: String) : LimitStatus()
        object Failure : LimitStatus()

    }
}