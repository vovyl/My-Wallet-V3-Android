package com.blockchain.kycui.email.validation

import com.blockchain.BaseKycPresenter
import com.blockchain.kyc.datamanagers.nabu.NabuDataManager
import com.blockchain.nabu.NabuToken
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import piuk.blockchain.androidcore.data.settings.EmailUpdater
import piuk.blockchain.kyc.R
import timber.log.Timber
import java.util.concurrent.TimeUnit

class KycEmailValidationPresenter(
    nabuToken: NabuToken,
    private val nabuDataManager: NabuDataManager,
    private val emailUpdater: EmailUpdater
) : BaseKycPresenter<KycEmailValidationView>(nabuToken) {

    override fun onViewReady() {
        compositeDisposable += Observable.interval(1, TimeUnit.SECONDS)
            .flatMapSingle {
                emailUpdater.email()
            }
            .observeOn(AndroidSchedulers.mainThread())
            .doOnError(Timber::e)
            .subscribe {
                if (it.verified) {
                    view.continueSignUp()
                }
            }

        compositeDisposable +=
            view.uiStateObservable
                .flatMapCompletable { (email, _) ->
                    emailUpdater.resendEmail()
                        .flatMapCompletable {
                            nabuDataManager.requestJwt()
                                .subscribeOn(Schedulers.io())
                                .flatMap { jwt ->
                                    fetchOfflineToken.flatMap { response ->
                                        nabuDataManager.updateUserWalletInfo(response, jwt)
                                            .subscribeOn(Schedulers.io())
                                    }
                                }
                                .ignoreElement()
                        }
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnSubscribe { view.showProgressDialog() }
                        .doOnTerminate { view.dismissProgressDialog() }
                        .doOnError {
                            Timber.e(it)
                            view.displayErrorDialog(R.string.kyc_email_validation_error_incorrect)
                        }
                        .doOnComplete { view.continueSignUp() }
                }
                .retry()
                .doOnError(Timber::e)
                .subscribe()
    }

    internal fun onProgressCancelled() {
        // Clear outbound requests
        compositeDisposable.clear()
        // Resubscribe
        onViewReady()
    }
}
