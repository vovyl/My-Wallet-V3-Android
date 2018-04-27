package piuk.blockchain.android.ui.buysell.coinify.signup.verify_email

import io.reactivex.Completable
import piuk.blockchain.androidcoreui.ui.base.View

interface CoinifyVerifyEmailView: View {

    fun onStartSignUpSuccess()

    fun onShowVerifiedEmail(emailAddress: String)

    fun onShowUnverifiedEmail(emailAddress: String)

    fun onShowErrorAndClose()

    fun onEnableContinueButton(emailVerified: Boolean)

    fun onCreateCoinifyAccount(verifiedEmailAddress: String): Completable

    fun onSignupError()
}