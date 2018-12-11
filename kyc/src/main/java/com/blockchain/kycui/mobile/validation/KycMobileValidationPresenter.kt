package com.blockchain.kycui.mobile.validation

import com.blockchain.BaseKycPresenter
import com.blockchain.kyc.datamanagers.nabu.NabuDataManager
import com.blockchain.nabu.NabuToken
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import piuk.blockchain.androidcore.data.settings.PhoneNumberUpdater
import piuk.blockchain.kyc.R
import timber.log.Timber

class KycMobileValidationPresenter(
    nabuToken: NabuToken,
    private val nabuDataManager: NabuDataManager,
    private val phoneNumberUpdater: PhoneNumberUpdater
) : BaseKycPresenter<KycMobileValidationView>(nabuToken) {

    override fun onViewReady() {
        compositeDisposable +=
            view.uiStateObservable
                .flatMapCompletable { (verificationModel, _) ->
                    phoneNumberUpdater.verifySms(verificationModel.verificationCode.code)
                        .flatMapCompletable {
                            nabuDataManager.requestJwt()
                                .subscribeOn(Schedulers.io())
                                .flatMap { jwt ->
                                    fetchOfflineToken.flatMap {
                                        nabuDataManager.updateUserWalletInfo(it, jwt)
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
                            view.displayErrorDialog(R.string.kyc_phone_number_validation_error_incorrect)
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
