package piuk.blockchain.android.ui.buysell.launcher

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
                .subscribe{ hasCoinifyAccount ->
                    if (hasCoinifyAccount) {
                        view.onStartCoinifyOverview()
                    } else {
                        view.onStartCoinifySignup()
                    }
                }
    }

    fun hasCoinifyAccountObservable() =
            exchangeService.getExchangeMetaData()
                .map {
                    it != null && it.coinify != null && it.coinify.token != null
                }
}