package piuk.blockchain.android.ui.buysell.launcher

import piuk.blockchain.androidcoreui.ui.base.BasePresenter
import javax.inject.Inject

class BuySellLauncherPresenter @Inject constructor(
): BasePresenter<BuySellLauncherView>() {

    override fun onViewReady() {

        // TODO This is fine for now since only Coinify countries will see native buy sell.
        view.onStartCoinify()
    }
}