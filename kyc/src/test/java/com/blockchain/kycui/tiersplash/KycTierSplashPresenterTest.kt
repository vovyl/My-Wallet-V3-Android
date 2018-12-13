package com.blockchain.kycui.tiersplash

import com.blockchain.android.testutils.rxInit
import com.blockchain.kyc.services.nabu.TierUpdater
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import io.reactivex.Completable
import org.amshove.kluent.`it returns`
import org.junit.Rule
import org.junit.Test
import piuk.blockchain.kyc.R

class KycTierSplashPresenterTest {

    @get:Rule
    val rxSchedulers = rxInit {
        mainTrampoline()
    }

    @Test
    fun `on tier1 selected`() {
        val view: KycTierSplashView = mock()
        val tierUpdater: TierUpdater = givenTierUpdater()
        KycTierSplashPresenter(tierUpdater)
            .also {
                it.onViewReady()
                it.initView(view)
            }
            .tier1Selected()
        verify(view).startEmailVerification()
        verify(tierUpdater).setUserTier(1)
    }

    @Test
    fun `on tier1 selected - error setting tier`() {
        val view: KycTierSplashView = mock()
        val tierUpdater: TierUpdater = givenUnableToSetTier()
        KycTierSplashPresenter(tierUpdater)
            .also {
                it.onViewReady()
                it.initView(view)
            }
            .tier1Selected()
        verify(tierUpdater).setUserTier(1)
        verify(view, never()).startEmailVerification()
        verify(view).showErrorToast(R.string.kyc_non_specific_server_error)
    }

    @Test
    fun `on tier2 selected`() {
        val view: KycTierSplashView = mock()
        val tierUpdater: TierUpdater = givenTierUpdater()
        KycTierSplashPresenter(tierUpdater)
            .also {
                it.onViewReady()
                it.initView(view)
            }
            .tier2Selected()
        verify(view).startEmailVerification()
        verify(tierUpdater).setUserTier(2)
    }

    @Test
    fun `on tier2 selected - error setting tier`() {
        val view: KycTierSplashView = mock()
        val tierUpdater: TierUpdater = givenUnableToSetTier()
        KycTierSplashPresenter(tierUpdater)
            .also {
                it.onViewReady()
                it.initView(view)
            }
            .tier2Selected()
        verify(tierUpdater).setUserTier(2)
        verify(view, never()).startEmailVerification()
        verify(view).showErrorToast(R.string.kyc_non_specific_server_error)
    }

    private fun givenTierUpdater(): TierUpdater =
        mock {
            on { setUserTier(any()) } `it returns` Completable.complete()
        }

    private fun givenUnableToSetTier(): TierUpdater =
        mock {
            on { setUserTier(any()) } `it returns` Completable.error(Throwable())
        }
}
