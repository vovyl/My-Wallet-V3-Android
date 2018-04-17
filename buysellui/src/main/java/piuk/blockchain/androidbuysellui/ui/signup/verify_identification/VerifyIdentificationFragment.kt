package piuk.blockchain.androidbuysellui.ui.signup.verify_identification

import android.content.Intent
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_verify_identification.*
import piuk.blockchain.androidbuysellui.R
import piuk.blockchain.androidbuysellui.injector.BuySellInjector
import piuk.blockchain.androidbuysellui.ui.signup.SignupActivity
import piuk.blockchain.androidcoreui.ui.base.BaseFragment
import piuk.blockchain.androidcoreui.utils.extensions.inflate
import javax.inject.Inject

class VerifyIdentificationFragment: BaseFragment<VerifyIdentificationView, VerifyIdentificationPresenter>(), VerifyIdentificationView {

    @Inject
    lateinit var presenter: VerifyIdentificationPresenter

    init {
        BuySellInjector.INSTANCE.presenterComponent.inject(this)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ) = container?.inflate(R.layout.fragment_verify_identification)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        verifyIdentificationNextButton.setOnClickListener { onStartOverview() }

        onViewReady()
    }

    private fun broadcastIntent(action: String) {
        activity?.run {
            LocalBroadcastManager.getInstance(this)
                    .sendBroadcast(Intent(action))
        }
    }

    override fun onStartOverview() {
        broadcastIntent(SignupActivity.ACTION_NAVIGATE_OVERVIEW)
    }

    override fun createPresenter() = presenter

    override fun getMvpView() = this

    companion object {

        @JvmStatic
        fun newInstance(): VerifyIdentificationFragment {
            return VerifyIdentificationFragment()
        }
    }
}