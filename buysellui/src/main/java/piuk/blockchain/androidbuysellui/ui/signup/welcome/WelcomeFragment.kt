package piuk.blockchain.androidbuysellui.ui.signup.welcome

import android.content.Intent
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_welcome.*
import piuk.blockchain.androidbuysellui.R
import piuk.blockchain.androidbuysellui.injector.BuySellInjector
import piuk.blockchain.androidbuysellui.ui.signup.SignupActivity
import piuk.blockchain.androidcoreui.ui.base.BaseFragment
import piuk.blockchain.androidcoreui.utils.extensions.inflate
import javax.inject.Inject

class WelcomeFragment: BaseFragment<WelcomeView, WelcomePresenter>(), WelcomeView {

    @Inject lateinit var presenter: WelcomePresenter

    init {
        BuySellInjector.INSTANCE.presenterComponent.inject(this)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ) = container?.inflate(R.layout.fragment_welcome)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getStartedButton.setOnClickListener { startCountrySelect() }

        onViewReady()
    }

    private fun broadcastIntent(action: String) {
        activity?.run {
            LocalBroadcastManager.getInstance(this)
                    .sendBroadcast(Intent(action))
        }
    }

    override fun createPresenter() = presenter

    override fun getMvpView() = this

    override fun startCountrySelect() {
        broadcastIntent(SignupActivity.ACTION_NAVIGATE_COUNTRY)
    }

    companion object {

        @JvmStatic
        fun newInstance(): WelcomeFragment {
            return WelcomeFragment()
        }
    }
}