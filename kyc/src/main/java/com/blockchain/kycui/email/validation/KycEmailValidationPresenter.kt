package com.blockchain.kycui.email.validation

import com.blockchain.kyc.datamanagers.nabu.NabuUserSync
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.plusAssign
import piuk.blockchain.androidcore.data.settings.EmailUpdater
import piuk.blockchain.androidcoreui.ui.base.BasePresenter
import piuk.blockchain.kyc.R
import timber.log.Timber
import java.util.concurrent.TimeUnit

class KycEmailValidationPresenter(
    private val nabuUserSync: NabuUserSync,
    private val emailUpdater: EmailUpdater
) : BasePresenter<KycEmailValidationView>() {

    override fun onViewReady() {
        compositeDisposable += Observable.interval(1, TimeUnit.SECONDS)
            .flatMapSingle {
                emailUpdater.email()
            }
            .distinctUntilChanged()
            .flatMapSingle {
                nabuUserSync.syncUser()
                    .andThen(Single.just(it))
            }
            .observeOn(AndroidSchedulers.mainThread())
            .retry()
            .doOnError(Timber::e)
            .subscribe {
                view.setVerified(it.verified)
            }

        compositeDisposable +=
            view.uiStateObservable
                .flatMapCompletable { (email, _) ->
                    emailUpdater.updateEmail(email)
                        .flatMapCompletable {
                            nabuUserSync.syncUser()
                        }
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnSubscribe { view.showProgressDialog() }
                        .doOnTerminate { view.dismissProgressDialog() }
                        .doOnError {
                            Timber.e(it)
                            view.displayErrorDialog(R.string.kyc_email_error_saving_email)
                        }
                        .doOnComplete {
                            view.theEmailWasResent()
                        }
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
