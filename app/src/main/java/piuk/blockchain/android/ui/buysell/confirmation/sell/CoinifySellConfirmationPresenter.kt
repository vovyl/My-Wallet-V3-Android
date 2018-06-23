package piuk.blockchain.android.ui.buysell.confirmation.sell

import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import piuk.blockchain.android.R
import piuk.blockchain.android.data.datamanagers.FeeDataManager
import piuk.blockchain.android.data.payments.SendDataManager
import piuk.blockchain.android.util.StringUtils
import piuk.blockchain.android.util.extensions.addToCompositeDisposable
import piuk.blockchain.androidbuysell.datamanagers.CoinifyDataManager
import piuk.blockchain.androidbuysell.models.coinify.BlockchainDetails
import piuk.blockchain.androidbuysell.models.coinify.CoinifyTradeRequest
import piuk.blockchain.androidbuysell.services.ExchangeService
import piuk.blockchain.androidbuysell.utils.fromIso8601
import piuk.blockchain.androidcore.data.payload.PayloadDataManager
import piuk.blockchain.androidcore.utils.extensions.applySchedulers
import piuk.blockchain.androidcoreui.ui.base.BasePresenter
import timber.log.Timber
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class CoinifySellConfirmationPresenter @Inject constructor(
        private val coinifyDataManager: CoinifyDataManager,
        private val exchangeService: ExchangeService,
        private val stringUtils: StringUtils,
        private val payloadDataManager: PayloadDataManager,
        private val sendDataManager: SendDataManager,
        private val feeDataManager: FeeDataManager
) : BasePresenter<CoinifySellConfirmationView>() {

    private val tokenSingle: Single<String>
        get() = exchangeService.getExchangeMetaData()
                .addToCompositeDisposable(this)
                .applySchedulers()
                .singleOrError()
                .map { it.coinify!!.token }

    override fun onViewReady() {
        val expiryDateGmt = view.displayableQuote.originalQuote.expiryTime.fromIso8601()!!
        val calendar = Calendar.getInstance()
        val timeZone = calendar.timeZone
        val offset = timeZone.getOffset(expiryDateGmt.time)

        startCountdown(expiryDateGmt.time + offset)
    }

    internal fun onConfirmClicked() {
        val displayModel = view.displayableQuote
        val quote = displayModel.originalQuote
        val bankAccountId = view.bankAccountId
        val account = payloadDataManager.accounts[displayModel.accountIndex]

        tokenSingle
                .addToCompositeDisposable(this)
                .applySchedulers()
                .flatMap {
                    coinifyDataManager.createNewTrade(
                            it,
                            CoinifyTradeRequest.sell(quote.id, bankAccountId)
                    )
                }
                .flatMapObservable { trade ->
                    sendDataManager.getUnspentOutputs(account.xpub)
                            .map {
                                sendDataManager.getSpendableCoins(
                                        it,
                                        displayModel.amountInSatoshis,
                                        displayModel.feePerKb
                                )
                            }
                            .flatMap { spendable ->
                                payloadDataManager.getNextChangeAddress(account)
                                        .flatMap {
                                            sendDataManager.submitBtcPayment(
                                                    spendable,
                                                    payloadDataManager.getHDKeysForSigning(
                                                            account,
                                                            spendable
                                                    ),
                                                    (trade.transferIn.details as BlockchainDetails).account,
                                                    it,
                                                    displayModel.absoluteFeeInSatoshis,
                                                    displayModel.amountInSatoshis
                                            )
                                        }
                            }
                }
                .subscribeBy(
                        onNext = {
                            view.showErrorDialog("Transaction sent successfully")
                        },
                        onError = {
                            Timber.e(it)
                            view.showErrorDialog("Oh shit son")
                        }
                )
    }

    private fun startCountdown(endTime: Long) {
        var remaining = (endTime - System.currentTimeMillis()) / 1000
        if (remaining <= 0) {
            // Finish page with error
            view.showQuoteExpiredDialog()
        } else {
            Observable.interval(1, TimeUnit.SECONDS)
                    .addToCompositeDisposable(this)
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnEach { remaining-- }
                    .map { return@map remaining }
                    .doOnNext {
                        val readableTime = String.format(
                                "%2d:%02d",
                                TimeUnit.SECONDS.toMinutes(it),
                                TimeUnit.SECONDS.toSeconds(it) -
                                        TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(it))
                        )
                        view.updateCounter(readableTime)
                    }
                    .doOnNext { if (it < 5 * 60) view.showTimeExpiring() }
                    .takeUntil { it <= 0 }
                    .doOnComplete { view.showQuoteExpiredDialog() }
                    .subscribe()
        }
    }

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
}
