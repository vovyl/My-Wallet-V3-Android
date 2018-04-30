package piuk.blockchain.android.ui.buysell.launcher

import io.reactivex.Observable
import piuk.blockchain.android.util.extensions.addToCompositeDisposable
import piuk.blockchain.androidbuysell.services.ExchangeService
import piuk.blockchain.androidcore.utils.extensions.applySchedulers
import piuk.blockchain.androidcoreui.ui.base.BasePresenter
import javax.inject.Inject

class BuySellLauncherPresenter @Inject constructor(
    private val exchangeService: ExchangeService
): BasePresenter<BuySellLauncherView>() {

    override fun onViewReady() {

        hasCoinifyAccountObservable()
                .applySchedulers()
                .addToCompositeDisposable(this)
                .subscribe {
                    if (it) {
                        view.onStartCoinifyOverview()
                    } else {
                        view.onStartCoinifySignup()
                    }
                }
    }

    private fun hasCoinifyAccountObservable(): Observable<Boolean> =
            exchangeService.getExchangeMetaData()
                    .applySchedulers()
                    .addToCompositeDisposable(this)
                    .map {
                        it.coinify?.token?.run {
                            true
                        } ?: false
                    }
}