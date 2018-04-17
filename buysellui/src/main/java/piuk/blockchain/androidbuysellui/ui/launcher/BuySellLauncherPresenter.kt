package piuk.blockchain.androidbuysellui.ui.launcher

import piuk.blockchain.androidcoreui.ui.base.BasePresenter
import javax.inject.Inject

class BuySellLauncherPresenter @Inject constructor(

): BasePresenter<BuySellLauncherView>() {

    override fun onViewReady() {

        // WIP - Check user signup status
        view.onSignup()
    }
}