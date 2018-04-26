package piuk.blockchain.android.ui.buysell.coinify.signup.verify_email

import android.support.annotation.VisibleForTesting
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import piuk.blockchain.android.util.extensions.addToCompositeDisposable
import piuk.blockchain.androidcore.data.settings.SettingsDataManager
import piuk.blockchain.androidcore.utils.extensions.applySchedulers
import piuk.blockchain.androidcoreui.ui.base.BasePresenter
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class CoinifyVerifyEmailPresenter @Inject constructor(
        private val settingsDataManager: SettingsDataManager
) : BasePresenter<CoinifyVerifyEmailView>() {

    private var verifiedEmailAddress: String? = null

    override fun onViewReady() {

        settingsDataManager.fetchSettings()
                .applySchedulers()
                .addToCompositeDisposable(this)
                .doOnError { view.onShowErrorAndClose() }
                .subscribe { settings ->

                    view.onEnableContinueButton(settings.isEmailVerified)

                    if (settings.isEmailVerified) {
                        setVerifiedEmailAndDisplay(settings.email)
                    } else {
                        view.onShowUnverifiedEmail(settings.email)
                        resendVerificationLink(settings.email)
                    }
                }
    }

    private fun resendVerificationLink(emailAddress: String) {
        settingsDataManager.updateEmail(emailAddress)
                .applySchedulers()
                .addToCompositeDisposable(this)
                .doOnError { view.onShowErrorAndClose() }
                .subscribe {
                    pollForEmailVerified()
                }
    }

    fun pollForEmailVerified() {
        Observable.interval(10, TimeUnit.SECONDS, Schedulers.io())
                .flatMap { settingsDataManager.fetchSettings() }
                .applySchedulers()
                .addToCompositeDisposable(this)
                .doOnNext {

                    view.onEnableContinueButton(it.isEmailVerified)

                    if (it.isEmailVerified) {
                        setVerifiedEmailAndDisplay(it.email)
                    }
                }
                .takeUntil { it.isEmailVerified }
                .subscribe {
                    //no-op
                }
    }

    @VisibleForTesting
    fun setVerifiedEmailAndDisplay(verifiedEmail: String) {
        verifiedEmailAddress = verifiedEmail
        view.onShowVerifiedEmail(verifiedEmail)
    }

    fun onContinueClicked() {
        verifiedEmailAddress?.run {
            view.onCreateCoinifyAccount(this)
                    .doOnComplete { view.onStartSignUpSuccess() }
                    .doOnError { view.onSignupError() }
                    .subscribe {
                        // no-op
                    }
        } ?: view.onShowErrorAndClose()
    }

    fun onTermsCheckChanged() {
        view.onEnableContinueButton(!verifiedEmailAddress.isNullOrEmpty())
    }
}