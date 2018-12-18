package com.blockchain.kycui.email.entry

import com.blockchain.BaseKycPresenter
import com.blockchain.kyc.datamanagers.nabu.NabuDataManager
import com.blockchain.nabu.NabuToken
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import piuk.blockchain.androidcore.data.settings.EmailUpdater
import piuk.blockchain.kyc.R
import timber.log.Timber

class KycEmailEntryPresenter(
    private val emailUpdater: EmailUpdater,
    private val nabuDataManager: NabuDataManager,
    nabuToken: NabuToken
) : BaseKycPresenter<KycEmailEntryView>(nabuToken) {

    override fun onViewReady() {
        preFillEmail()
        subscribeToClickEvents()
    }

    private fun subscribeToClickEvents() {
        compositeDisposable +=
            view.uiStateObservable
                .map { it.first }
                .flatMapCompletable { email ->
                    emailUpdater.updateEmail(email)
                        .flatMapCompletable {
                            nabuDataManager.requestJwt()
                                .subscribeOn(Schedulers.io())
                                .flatMap { jwt ->
                                    fetchOfflineToken.flatMap { offlineToken ->
                                        nabuDataManager.updateUserWalletInfo(offlineToken, jwt)
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
                            view.showErrorToast(R.string.kyc_email_error_saving_email)
                        }
                        .doOnComplete {
                            view.continueSignUp(email)
                        }
                }
                .retry()
                .doOnError(Timber::e)
                .subscribe()
    }

    private fun preFillEmail() {
        compositeDisposable +=
            emailUpdater.email()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                    onSuccess = {
                        view.preFillEmail(it.address)
                    },
                    // Ignore error
                    onError = { Timber.e(it) }
                )
    }

    internal fun onProgressCancelled() {
        // Cancel outbound
        compositeDisposable.clear()
        // Resubscribe to everything
        subscribeToClickEvents()
    }
}
