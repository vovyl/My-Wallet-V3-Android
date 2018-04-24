package piuk.blockchain.android.ui.buysell.coinify.signup.verify_email

import android.content.Intent
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_coinify_verify_email.*
import piuk.blockchain.android.R
import piuk.blockchain.android.injection.Injector
import piuk.blockchain.android.ui.buysell.coinify.signup.CoinifySignupActivity
import piuk.blockchain.androidcoreui.ui.base.BaseFragment
import piuk.blockchain.androidcoreui.utils.extensions.inflate
import javax.inject.Inject

class CoinifyVerifyEmailFragment: BaseFragment<CoinifyVerifyEmailView, CoinifyVerifyEmailPresenter>(), CoinifyVerifyEmailView {

    @Inject
    lateinit var presenter: CoinifyVerifyEmailPresenter

    init {
        Injector.INSTANCE.presenterComponent.inject(this)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ) = container?.inflate(R.layout.fragment_coinify_verify_email)

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
        broadcastIntent(CoinifySignupActivity.ACTION_NAVIGATE_CREATE_ACCOUNT_COMPLETED)
    }

    override fun createPresenter() = presenter

    override fun getMvpView() = this

    companion object {

        @JvmStatic
        fun newInstance(): CoinifyVerifyEmailFragment {
            return CoinifyVerifyEmailFragment()
        }
    }
}