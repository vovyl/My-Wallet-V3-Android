package piuk.blockchain.android.ui.buysell.confirmation

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import piuk.blockchain.android.util.extensions.addToCompositeDisposable
import piuk.blockchain.androidbuysell.utils.fromIso8601
import piuk.blockchain.androidcoreui.ui.base.BasePresenter
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class CoinifyOrderConfirmationPresenter @Inject constructor(

) : BasePresenter<CoinifyOrderConfirmationView>() {

    override fun onViewReady() {
        val expiryDateGmt = view.displayableQuote.originalQuote.expiryTime.fromIso8601()
        val calendar = Calendar.getInstance()
        val timeZone = calendar.timeZone
        val offset = timeZone.getOffset(expiryDateGmt!!.time)

        startCountdown(expiryDateGmt.time + offset)
    }
    
    internal fun onConfirmClicked() {
        // TODO:   
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