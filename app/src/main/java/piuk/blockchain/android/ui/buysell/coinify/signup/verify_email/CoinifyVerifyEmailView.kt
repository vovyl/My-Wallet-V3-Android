package piuk.blockchain.android.ui.buysell.coinify.signup.verify_email

import piuk.blockchain.androidcoreui.ui.base.View

interface CoinifyVerifyEmailView: View {

    fun onStartSignUpSuccess()

    fun onShowVerifiedEmail(emailAddress: String)

    fun onShowUnverifiedEmail(emailAddress: String)

    fun onShowErrorAndClose()
}