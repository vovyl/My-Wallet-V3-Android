package piuk.blockchain.android.ui.buysell.launcher

import io.reactivex.Observable
import piuk.blockchain.android.util.extensions.addToCompositeDisposable
import piuk.blockchain.androidbuysell.services.ExchangeService
import piuk.blockchain.androidcore.utils.extensions.applySchedulers
import piuk.blockchain.androidcoreui.ui.base.BasePresenter
import timber.log.Timber
import javax.inject.Inject

class BuySellLauncherPresenter @Inject constructor(
        private val exchangeService: ExchangeService
) : BasePresenter<BuySellLauncherView>() {

    override fun onViewReady() {
        hasCoinifyAccountObservable()
                .addToCompositeDisposable(this)
                .applySchedulers()
                .subscribe(
                        { hasCoinifyAccount ->
                            if (hasCoinifyAccount) {
                                view.onStartCoinifyOverview()
                            } else {
                                view.onStartCoinifySignUp()
                            }

                        },
                        {
                            Timber.e(it)
                            view.finishPage()
                        }
                )
    }

    private fun hasCoinifyAccountObservable(): Observable<Boolean> =
            exchangeService.getExchangeMetaData()
                    .map { it.coinify != null && it.coinify.token != null }
}