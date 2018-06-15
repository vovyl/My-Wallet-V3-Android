package piuk.blockchain.android.ui.buysell.overview

import android.support.annotation.StringRes
import io.reactivex.Observable
import io.reactivex.rxkotlin.subscribeBy
import piuk.blockchain.android.R
import piuk.blockchain.android.ui.buysell.details.models.AwaitingFundsModel
import piuk.blockchain.android.ui.buysell.details.models.BuySellDetailsModel
import piuk.blockchain.android.ui.buysell.overview.models.BuySellButtons
import piuk.blockchain.android.ui.buysell.overview.models.BuySellDisplayable
import piuk.blockchain.android.ui.buysell.overview.models.BuySellTransaction
import piuk.blockchain.android.ui.buysell.overview.models.EmptyTransactionList
import piuk.blockchain.android.ui.buysell.overview.models.KycInProgress
import piuk.blockchain.android.util.StringUtils
import piuk.blockchain.android.util.extensions.addToCompositeDisposable
import piuk.blockchain.android.util.extensions.toFormattedString
import piuk.blockchain.androidbuysell.datamanagers.CoinifyDataManager
import piuk.blockchain.androidbuysell.models.coinify.BankDetails
import piuk.blockchain.androidbuysell.models.coinify.CoinifyTrade
import piuk.blockchain.androidbuysell.models.coinify.KycResponse
import piuk.blockchain.androidbuysell.models.coinify.Medium
import piuk.blockchain.androidbuysell.models.coinify.ReviewState
import piuk.blockchain.androidbuysell.models.coinify.TradeState
import piuk.blockchain.androidbuysell.services.ExchangeService
import piuk.blockchain.androidbuysell.utils.fromIso8601
import piuk.blockchain.androidcore.data.currency.BTCDenomination
import piuk.blockchain.androidcore.data.currency.CurrencyFormatManager
import piuk.blockchain.androidcore.utils.extensions.applySchedulers
import piuk.blockchain.androidcore.utils.helperfunctions.unsafeLazy
import piuk.blockchain.androidcoreui.ui.base.BasePresenter
import timber.log.Timber
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*
import javax.inject.Inject

class CoinifyOverviewPresenter @Inject constructor(
        private val exchangeService: ExchangeService,
        private val coinifyDataManager: CoinifyDataManager,
        private val currencyFormatManager: CurrencyFormatManager,
        private val stringUtils: StringUtils
) : BasePresenter<CoinifyOverviewView>() {

    // Display States
    private val buttons = BuySellButtons()
    private val kycInReview = KycInProgress()
    private val empty = EmptyTransactionList()
    // Display List
    private val displayList: MutableList<BuySellDisplayable> = mutableListOf(buttons)
    // Observables
    private val kycReviewsObservable: Observable<Boolean> by unsafeLazy {
        exchangeService.getExchangeMetaData()
                .addToCompositeDisposable(this)
                .applySchedulers()
                .map { it.coinify!!.token }
                .flatMapSingle { coinifyDataManager.getKycReviews(it) }
                .map { it.hasPendingKyc() }
                .cache()
    }

    override fun onViewReady() {
        renderTrades(emptyList())
        view.renderViewState(OverViewState.Loading)
        refreshTransactionList()
        checkKycStatus()
    }

    internal fun refreshTransactionList() {
        getTradesObservable()
                .map { mapTradeToDisplayObject(it) }
                .toList()
                .doOnError { Timber.e(it) }
                .subscribeBy(
                        onSuccess = { renderTrades(it) },
                        onError = {
                            view.renderViewState(OverViewState.Failure(R.string.buy_sell_overview_error_loading_transactions))
                        }
                )
    }

    internal fun onBuySelected() {
        kycReviewsObservable
                .doOnSubscribe { view.displayProgressDialog() }
                .doAfterTerminate { view.dismissProgressDialog() }
                .subscribeBy(
                        onNext = { hasPendingKyc ->
                            if (hasPendingKyc) {
                                view.launchCardBuyFlow()
                            } else {
                                view.launchBuyPaymentSelectionFlow()
                            }
                        },
                        onError = {
                            view.renderViewState(OverViewState.Failure(R.string.unexpected_error))
                        }
                )
    }

    internal fun onSellSelected() {
        kycReviewsObservable
                .doOnSubscribe { view.displayProgressDialog() }
                .doAfterTerminate { view.dismissProgressDialog() }
                .subscribeBy(
                        onNext = { hasPendingKyc ->
                            if (hasPendingKyc) {
                                view.showAlertDialog(R.string.buy_sell_overview_sell_unavailable)
                            } else {
                                view.launchSellFlow()
                            }
                        },
                        onError = {
                            view.renderViewState(OverViewState.Failure(R.string.unexpected_error))
                        }
                )
    }

    internal fun onTransactionSelected(transactionId: Int) {
        getTradesObservable()
                .doOnSubscribe { view.displayProgressDialog() }
                .filter { it.id == transactionId }
                .firstOrError()
                .doOnEvent { _, _ -> view.dismissProgressDialog() }
                .subscribeBy(
                        onSuccess = {
                            if (it.isAwaitingTransferIn()) {
                                view.launchAwaitingTransferPage(getAwaitingFundsModel(it))
                            } else {
                                view.launchDetailsPage(getBuySellDetailsModel(it))
                            }
                        },
                        onError = {
                            view.renderViewState(OverViewState.Failure(R.string.buy_sell_overview_error_loading_transactions))
                        }
                )
    }

    private fun getAwaitingFundsModel(coinifyTrade: CoinifyTrade): AwaitingFundsModel {
        val (referenceText, account, bank, holder, _, _) = coinifyTrade.transferIn.details as BankDetails
        val formattedAmount = formatFiatWithSymbol(
                coinifyTrade.transferIn.sendAmount,
                coinifyTrade.transferIn.currency,
                view.locale
        )

        return AwaitingFundsModel(
                formattedAmount,
                referenceText,
                holder.name,
                holder.address.getFormattedAddressString(),
                account.number,
                account.bic,
                "${bank.name}, ${bank.address.getFormattedAddressString()}"
        )
    }

    private fun checkKycStatus() {
        kycReviewsObservable
                .subscribeBy(
                        onNext = { hasPendingKyc ->
                            if (hasPendingKyc) {
                                displayList.add(0, kycInReview)
                                view.renderViewState(OverViewState.Data(displayList.toList()))
                            }
                        },
                        onError = { Timber.e(it) }
                )
    }

    private fun renderTrades(trades: List<BuySellTransaction>) {
        displayList.removeAll { it is BuySellTransaction || it is EmptyTransactionList }
        displayList.apply { addAll(trades) }
                .apply {
                    if (trades.isEmpty()) {
                        add(empty)
                    } else {
                        removeAll { it is EmptyTransactionList }
                    }
                }
        view.renderViewState(OverViewState.Data(displayList.toList()))
    }

    @StringRes
    private fun tradeStateToStringRes(state: TradeState): Int = when (state) {
        TradeState.AwaitingTransferIn -> R.string.buy_sell_state_awaiting_funds
        TradeState.Completed -> R.string.buy_sell_state_completed
        TradeState.Cancelled -> R.string.buy_sell_state_cancelled
        TradeState.Rejected -> R.string.buy_sell_state_rejected
        TradeState.Expired -> R.string.buy_sell_state_expired
        TradeState.Processing, TradeState.Reviewing -> R.string.buy_sell_state_processing
    }

    private fun getTradesObservable(): Observable<CoinifyTrade> =
            exchangeService.getExchangeMetaData()
                    .addToCompositeDisposable(this)
                    .applySchedulers()
                    .map { it.coinify!!.token }
                    .flatMap { coinifyDataManager.getTrades(it) }

    //region Model helper functions
    private fun mapTradeToDisplayObject(coinifyTrade: CoinifyTrade): BuySellTransaction {
        val displayString = if (coinifyTrade.isSellTransaction()) {
            "-${coinifyTrade.inAmount} ${coinifyTrade.inCurrency.capitalize()}"
        } else {
            val amount = coinifyTrade.outAmount ?: coinifyTrade.outAmountExpected
            "+$amount ${coinifyTrade.outCurrency.capitalize()}"
        }

        return BuySellTransaction(
                transactionId = coinifyTrade.id,
                time = coinifyTrade.createTime.fromIso8601()!!,
                displayAmount = displayString,
                tradeStateString = tradeStateToStringRes(coinifyTrade.state),
                tradeState = coinifyTrade.state,
                isSellTransaction = coinifyTrade.isSellTransaction()
        )
    }

    private fun getBuySellDetailsModel(coinifyTrade: CoinifyTrade): BuySellDetailsModel {
        // Title
        val stateString = stringUtils.getString(tradeStateToStringRes(coinifyTrade.state))
        val titleStringRes =
                if (coinifyTrade.isSellTransaction()) {
                    R.string.buy_sell_detail_title_sell
                } else {
                    R.string.buy_sell_detail_title_buy
                }
        val titleString = stringUtils.getFormattedString(titleStringRes, stateString)
        // Date
        val dateString =
                (coinifyTrade.updateTime.fromIso8601() ?: Date()).toFormattedString(view.locale)
        // Amounts
        val sent = coinifyTrade.transferIn.receiveAmount
        val sentWithFee = coinifyTrade.transferIn.sendAmount
        val received = coinifyTrade.transferOut.sendAmount
        val paymentFee = coinifyTrade.transferIn.getFee().toBigDecimal()
                .setScale(8, RoundingMode.HALF_UP)
                .abs()
                .stripTrailingZeros()
        // Currency
        val outCurrency = coinifyTrade.transferOut.currency.capitalize()
        val inCurrency = coinifyTrade.transferIn.currency.capitalize()
        val isEndState = coinifyTrade.state.isEndState()
        // Model Strings
        val receiveString: String
        val paymentFeeString: String
        val exchangeRateString: String
        val receiveTitleString: String
        val amountString: String
        val totalString: String

        if (!coinifyTrade.isSellTransaction()) {
            // Crypto out (from Coinify's perspective)
            receiveString = "$received $outCurrency"
            // Exchange rate (always in fiat)
            val exchangeRate = sent / received
            exchangeRateString = formatFiatWithSymbol(exchangeRate, inCurrency, view.locale)
            // Fiat in
            amountString = formatFiatWithSymbol(sent, inCurrency, view.locale)
            paymentFeeString = formatFiatWithSymbol(paymentFee.toDouble(), inCurrency, view.locale)
            totalString = formatFiatWithSymbol(sentWithFee, inCurrency, view.locale)
            // Received/Sold title
            receiveTitleString = getReceiveTitleString(
                    isEndState,
                    R.string.buy_sell_detail_currency_to_be_received,
                    R.string.buy_sell_detail_currency_received,
                    outCurrency
            )
        } else {
            // Fiat out (from Coinify's perspective)
            receiveString = formatFiatWithSymbol(received, outCurrency, view.locale)
            // Exchange rate (always in fiat)
            val exchangeRate = received / sent
            exchangeRateString = formatFiatWithSymbol(exchangeRate, outCurrency, view.locale)
            // Crypto in
            val formattedReceived = currencyFormatManager.getFormattedBtcValue(
                    received.toBigDecimal(),
                    BTCDenomination.SATOSHI
            )
            amountString = "$formattedReceived $inCurrency"
            val formattedFee = currencyFormatManager.getFormattedBtcValue(
                    paymentFee,
                    BTCDenomination.SATOSHI
            )
            paymentFeeString = "$formattedFee $inCurrency"
            totalString = "$sentWithFee $inCurrency"
            // Received/Sold title
            receiveTitleString = getReceiveTitleString(
                    isEndState,
                    R.string.buy_sell_detail_currency_to_be_sold,
                    R.string.buy_sell_detail_currency_sold,
                    inCurrency
            )
        }

        return BuySellDetailsModel(
                coinifyTrade.isSellTransaction(),
                coinifyTrade.isAwaitingCardPayment(),
                titleString,
                receiveString,
                dateString,
                "#${coinifyTrade.id}",
                coinifyTrade.id,
                receiveTitleString,
                exchangeRateString,
                amountString,
                paymentFeeString,
                totalString
        )
    }
    //endregion

    //region Formatting helpers
    private fun formatFiatWithSymbol(
            fiatValue: Double,
            currencyCode: String,
            locale: Locale
    ): String {
        val numberFormat = NumberFormat.getCurrencyInstance(locale)
        val decimalFormatSymbols = (numberFormat as DecimalFormat).decimalFormatSymbols
        numberFormat.decimalFormatSymbols = decimalFormatSymbols.apply {
            this.currencySymbol = Currency.getInstance(currencyCode).getSymbol(locale)
        }
        return numberFormat.format(fiatValue)
    }

    private fun getReceiveTitleString(
            isEndState: Boolean,
            @StringRes pendingString: Int,
            @StringRes completeString: Int,
            currencyCode: String
    ): String = stringUtils.getFormattedString(
            if (isEndState) completeString else pendingString,
            currencyCode.capitalize()
    )
    //endregion

    //region Extension functions
    private fun List<KycResponse>.hasPendingKyc(): Boolean =
            this.any { it.state.isProcessing() }
                    && this.none { it.state == ReviewState.Completed }

    private fun CoinifyTrade.isAwaitingTransferIn(): Boolean =
            (!this.isSellTransaction()
                    && this.state == TradeState.AwaitingTransferIn
                    && this.transferIn.medium == Medium.Bank)

    private fun CoinifyTrade.isAwaitingCardPayment(): Boolean =
            (!this.isSellTransaction()
                    && this.state == TradeState.AwaitingTransferIn
                    && this.transferIn.medium == Medium.Card)
    //endregion
}

sealed class OverViewState {

    object Loading : OverViewState()
    class Failure(@StringRes val message: Int) : OverViewState()
    class Data(val items: List<BuySellDisplayable>) : OverViewState()

}