package com.blockchain.kycui.tiersplash

import com.blockchain.kyc.services.nabu.TierUpdater
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import piuk.blockchain.androidcoreui.ui.base.BasePresenter
import piuk.blockchain.kyc.R
import timber.log.Timber

class KycTierSplashPresenter(
    private val tierUpdater: TierUpdater
) : BasePresenter<KycTierSplashView>() {

    override fun onViewReady() {
    }

    fun tier1Selected() {
        navigateToTier(1)
    }

    fun tier2Selected() {
        navigateToTier(2)
    }

    private fun navigateToTier(tier: Int) {
        compositeDisposable += tierUpdater.setUserTier(tier)
            .observeOn(AndroidSchedulers.mainThread())
            .doOnError(Timber::e)
            .subscribeBy(
                onComplete = {
                    view!!.startEmailVerification()
                },
                onError = {
                    view!!.showErrorToast(R.string.kyc_non_specific_server_error)
                }
            )
    }
}
