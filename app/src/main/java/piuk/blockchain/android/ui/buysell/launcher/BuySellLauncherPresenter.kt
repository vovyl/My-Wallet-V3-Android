package piuk.blockchain.android.ui.buysell.launcher

import piuk.blockchain.androidbuysell.services.ExchangeService
import piuk.blockchain.androidcoreui.ui.base.BasePresenter
import javax.inject.Inject

class BuySellLauncherPresenter @Inject constructor(
    private val exchangeService: ExchangeService
): BasePresenter<BuySellLauncherView>() {

    override fun onViewReady() {

        view.onStartCoinifySignup()
    }
}