package piuk.blockchain.android.ui.buysell.confirmation

import android.view.Display
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import piuk.blockchain.android.ui.buysell.createorder.models.OrderType
import piuk.blockchain.android.util.extensions.addToCompositeDisposable
import piuk.blockchain.androidbuysell.datamanagers.CoinifyDataManager
import piuk.blockchain.androidbuysell.models.coinify.CardDetails
import piuk.blockchain.androidbuysell.models.coinify.CoinifyTradeRequest
import piuk.blockchain.androidbuysell.models.coinify.exceptions.CoinifyApiException
import piuk.blockchain.androidbuysell.models.coinify.exceptions.CoinifyErrorCodes
import piuk.blockchain.androidbuysell.services.ExchangeService
import piuk.blockchain.androidbuysell.utils.fromIso8601
import piuk.blockchain.androidcore.utils.extensions.applySchedulers
import piuk.blockchain.androidcoreui.ui.base.BasePresenter
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class CoinifyOrderConfirmationPresenter @Inject constructor(
        private val coinifyDataManager: CoinifyDataManager,
        private val exchangeService: ExchangeService
) : BasePresenter<CoinifyOrderConfirmationView>() {

    private val tokenSingle: Single<String>
        get() = exchangeService.getExchangeMetaData()
                .addToCompositeDisposable(this)
                .applySchedulers()
                .singleOrError()
                .map { it.coinify!!.token }

    override fun onViewReady() {
        val expiryDateGmt = view.displayableQuote.originalQuote.expiryTime.fromIso8601()
        val calendar = Calendar.getInstance()
        val timeZone = calendar.timeZone
        val offset = timeZone.getOffset(expiryDateGmt!!.time)

        startCountdown(expiryDateGmt.time + offset)
    }

    internal fun onConfirmClicked() {
        // TODO: this is only for card buy
        if (view.orderType == OrderType.BuyCard) {
            tokenSingle.flatMap {
                val quote = view.displayableQuote
                return@flatMap coinifyDataManager.createNewTrade(
                        it,
                        CoinifyTradeRequest.cardBuy(
                                quote.originalQuote.id,
                                quote.bitcoinAddress!!
                        )
                )
            }.doOnSubscribe { view.displayProgressDialog() }
                    .doAfterTerminate { view.dismissProgressDialog() }
                    .subscribeBy(
                            onSuccess = {
                                val redirectUrl = (it.transferIn.details as CardDetails).redirectUrl
                                view.launchCardPaymentWebView(redirectUrl)
                            },
                            onError = {
                                Timber.e(it)
                                if (it is CoinifyApiException) {
                                        // TODO: Display error message?
                                } else {
                                    // TODO: Display generic error
                                }
                            }
                    )
        } else {
            TODO()
        }

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

}