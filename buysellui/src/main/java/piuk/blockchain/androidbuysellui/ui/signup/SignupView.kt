package piuk.blockchain.androidbuysellui.ui.signup

import piuk.blockchain.androidcoreui.ui.base.View

interface SignupView: View {

    fun onStartWelcome()

    fun onStartCountrySelect()

    fun onStartVerifyEmail()

    fun onStartVerifyIdentity()
}