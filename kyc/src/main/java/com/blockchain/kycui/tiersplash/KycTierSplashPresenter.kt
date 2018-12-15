package com.blockchain.kycui.tiersplash

import com.blockchain.kyc.models.nabu.KycTierState
import com.blockchain.kyc.services.nabu.TierService
import com.blockchain.kyc.services.nabu.TierUpdater
import io.reactivex.Maybe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import piuk.blockchain.androidcoreui.ui.base.BasePresenter
import piuk.blockchain.kyc.R
import timber.log.Timber

class KycTierSplashPresenter(
    private val tierUpdater: TierUpdater,
    private val tierService: TierService
) : BasePresenter<KycTierSplashView>() {

    override fun onViewReady() {
        compositeDisposable +=
            tierService.tiers()
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError(Timber::e)
                .subscribeBy(
                    onSuccess = {
                        view!!.renderTiersList(it)
                    },
                    onError = {
                        view!!.showErrorToast(R.string.kyc_non_specific_server_error)
                    }
                )
    }

    fun tier1Selected() {
        navigateToTier(1)
    }

    fun tier2Selected() {
        navigateToTier(2)
    }

    private fun navigateToTier(tier: Int) {
        compositeDisposable += tierService.tiers()
            .map { it.tiers[tier] }
            .filter { it.state != KycTierState.Verified }
            .flatMap {
                tierUpdater.setUserTier(tier)
                    .andThen(Maybe.just(tier))
            }
            .observeOn(AndroidSchedulers.mainThread())
            .doOnError(Timber::e)
            .subscribeBy(
                onSuccess = {
                    view!!.startEmailVerification()
                },
                onError = {
                    view!!.showErrorToast(R.string.kyc_non_specific_server_error)
                }
            )
    }
}
