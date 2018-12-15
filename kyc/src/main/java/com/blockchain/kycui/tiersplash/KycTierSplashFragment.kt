package com.blockchain.kycui.tiersplash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.NavHostFragment.findNavController
import com.blockchain.kycui.navhost.KycProgressListener
import com.blockchain.kycui.navhost.models.CampaignType
import com.blockchain.kycui.navhost.models.KycStep
import com.blockchain.notifications.analytics.EventLogger
import com.blockchain.notifications.analytics.LoggableEvent
import com.blockchain.ui.extensions.throttledClicks
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject
import piuk.blockchain.androidcoreui.ui.base.BaseFragment
import piuk.blockchain.androidcoreui.ui.customviews.ToastCustom
import piuk.blockchain.androidcoreui.utils.ParentActivityDelegate
import piuk.blockchain.androidcoreui.utils.extensions.inflate
import piuk.blockchain.androidcoreui.utils.extensions.toast
import piuk.blockchain.kyc.R
import timber.log.Timber

class KycTierSplashFragment : BaseFragment<KycTierSplashView, KycTierSplashPresenter>(),
    KycTierSplashView {

    private val presenter: KycTierSplashPresenter by inject()

    private val progressListener: KycProgressListener by ParentActivityDelegate(this)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = container?.inflate(R.layout.fragment_kyc_tier_splash)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        get<EventLogger>().logEvent(LoggableEvent.KycTiers)

        val title = when (progressListener.campaignType) {
            CampaignType.Swap -> R.string.kyc_splash_title
            CampaignType.Sunriver -> R.string.sunriver_splash_title
        }

        progressListener.setHostTitle(title)
        progressListener.incrementProgress(KycStep.SplashPage)
    }

    private val disposable = CompositeDisposable()

    override fun onResume() {
        super.onResume()
        disposable += view!!.findViewById<View>(R.id.card_tier_1)
            .throttledClicks()
            .subscribeBy(
                onNext = {
                    presenter.tier1Selected()
                },
                onError = { Timber.e(it) }
            )
        disposable += view!!.findViewById<View>(R.id.card_tier_2)
            .throttledClicks()
            .subscribeBy(
                onNext = {
                    presenter.tier2Selected()
                },
                onError = { Timber.e(it) }
            )
    }

    override fun onPause() {
        disposable.clear()
        super.onPause()
    }

    override fun createPresenter() = presenter

    override fun getMvpView() = this

    override fun startEmailVerification() {
        findNavController(this).navigate(R.id.email_verification)
    }

    override fun showErrorToast(message: Int) {
        toast(message, ToastCustom.TYPE_ERROR)
    }
}
