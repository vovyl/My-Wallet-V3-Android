package piuk.blockchain.androidbuysellui.ui.signup

import piuk.blockchain.androidcoreui.ui.base.BasePresenter
import javax.inject.Inject

class SignupPresenter @Inject constructor(

) : BasePresenter<SignupView>() {

    override fun onViewReady() {

        // WIP - Start specific step of account creation
        // or select country
        // or verify email
        // or verify identity
        view.onStartWelcome()

        // Or if user has account
//        view.onStartOverview()
    }

}