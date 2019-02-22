package com.blockchain.kycui.resubmission

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.blockchain.kycui.navhost.KycProgressListener
import com.blockchain.kycui.navhost.models.KycStep
import com.blockchain.kycui.navigate
import com.blockchain.kycui.reentry.KycNavigator
import com.blockchain.notifications.analytics.LoggableEvent
import com.blockchain.notifications.analytics.logEvent
import com.blockchain.ui.extensions.throttledClicks
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.koin.android.ext.android.inject
import piuk.blockchain.androidcoreui.utils.ParentActivityDelegate
import piuk.blockchain.androidcoreui.utils.extensions.inflate
import piuk.blockchain.kyc.R
import timber.log.Timber
import kotlinx.android.synthetic.main.fragment_kyc_resubmission_splash
    .button_kyc_resubmission_splash_next as buttonContinue

class KycResubmissionSplashFragment : Fragment() {

    private val progressListener: KycProgressListener by ParentActivityDelegate(this)

    private val kycNavigator: KycNavigator by inject()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = container?.inflate(R.layout.fragment_kyc_resubmission_splash)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        logEvent(LoggableEvent.KycResubmission)

        progressListener.setHostTitle(R.string.kyc_resubmission_splash_title)
        progressListener.incrementProgress(KycStep.SplashPage)
    }

    private val disposable = CompositeDisposable()

    override fun onResume() {
        super.onResume()
        disposable += buttonContinue
            .throttledClicks()
            .flatMapSingle { kycNavigator.findNextStep() }
            .subscribeBy(
                onNext = { navigate(it) },
                onError = { Timber.e(it) }
            )
    }

    override fun onPause() {
        disposable.clear()
        super.onPause()
    }
}
