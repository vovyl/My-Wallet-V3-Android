package piuk.blockchain.android.ui.buysell.coinify.signup

import piuk.blockchain.androidcoreui.ui.base.BasePresenter
import javax.inject.Inject

class CoinifySignupPresenter @Inject constructor(

) : BasePresenter<CoinifySignupView>() {

    override fun onViewReady() {

        // WIP - Start specific step of account creation
        // or select country
        // or verify email
        // or verify identity
        view.onStartWelcome()

        // Or if user has account
//        view.onStartCoinifyOverview()
    }

}