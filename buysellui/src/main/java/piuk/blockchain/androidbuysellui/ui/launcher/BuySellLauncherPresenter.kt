package piuk.blockchain.androidbuysellui.ui.launcher

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
                        view.onStartOverview()
                    } else {
                        view.onStartSignup()
                    }
                }
    }

    fun hasCoinifyAccountObservable() =
            exchangeService.getExchangeMetaData()
                .map {
                    it != null && it.coinify != null && it.coinify.token != null
                }
}