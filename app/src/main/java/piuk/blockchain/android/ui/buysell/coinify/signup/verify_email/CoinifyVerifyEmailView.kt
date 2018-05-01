package piuk.blockchain.android.ui.buysell.coinify.signup.verify_email

import io.reactivex.Observable
import piuk.blockchain.androidbuysell.models.coinify.KycResponse
import piuk.blockchain.androidcoreui.ui.base.View

interface CoinifyVerifyEmailView: View {

    fun onStartSignUpSuccess()

    fun onShowVerifiedEmail(emailAddress: String)

    fun onShowUnverifiedEmail(emailAddress: String)

    fun onShowErrorAndClose()

    fun onEnableContinueButton(emailVerified: Boolean)

    fun onCreateCoinifyAccount(verifiedEmailAddress: String): Observable<KycResponse>

    fun onSignupError()
}