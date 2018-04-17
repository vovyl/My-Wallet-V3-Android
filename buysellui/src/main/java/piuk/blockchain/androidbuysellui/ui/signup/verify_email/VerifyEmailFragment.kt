package piuk.blockchain.androidbuysellui.ui.signup.verify_email

import android.content.Intent
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_verify_email.*
import piuk.blockchain.androidbuysellui.R
import piuk.blockchain.androidbuysellui.injector.BuySellInjector
import piuk.blockchain.androidbuysellui.ui.signup.SignupActivity
import piuk.blockchain.androidcoreui.ui.base.BaseFragment
import piuk.blockchain.androidcoreui.utils.extensions.inflate
import javax.inject.Inject

class VerifyEmailFragment: BaseFragment<VerifyEmailView, VerifyEmailPresenter>(), VerifyEmailView {

    @Inject
    lateinit var presenter: VerifyEmailPresenter

    init {
        BuySellInjector.INSTANCE.presenterComponent.inject(this)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ) = container?.inflate(R.layout.fragment_verify_email)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        verifyIdentificationButton.setOnClickListener { onStartCreateAccountCompleted() }

        onViewReady()
    }

    private fun broadcastIntent(action: String) {
        activity?.run {
            LocalBroadcastManager.getInstance(this)
                    .sendBroadcast(Intent(action))
        }
    }

    override fun onStartCreateAccountCompleted() {
        broadcastIntent(SignupActivity.ACTION_NAVIGATE_CREATE_ACCOUNT_COMPLETED)
    }

    override fun createPresenter() = presenter

    override fun getMvpView() = this

    companion object {

        @JvmStatic
        fun newInstance(): VerifyEmailFragment {
            return VerifyEmailFragment()
        }
    }
}