package piuk.blockchain.android.ui.buysell.coinify.signup.create_account_completed

import android.content.Intent
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_coinify_create_account_completed.*
import piuk.blockchain.android.R
import piuk.blockchain.android.injection.Injector
import piuk.blockchain.android.ui.buysell.coinify.signup.CoinifySignupActivity
import piuk.blockchain.androidcoreui.ui.base.BaseFragment
import piuk.blockchain.androidcoreui.utils.extensions.inflate
import javax.inject.Inject

class CoinifyCreateAccountCompletedFragment: BaseFragment<CoinifyCreateAccountCompletedView, CoinifyCreateAccountCompletedPresenter>(), CoinifyCreateAccountCompletedView {

    @Inject
    lateinit var presenter: CoinifyCreateAccountCompletedPresenter

    init {
        Injector.INSTANCE.presenterComponent.inject(this)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ) = container?.inflate(R.layout.fragment_coinify_create_account_completed)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        createAccountCompletedContinueButton.setOnClickListener { onStartVerifyIdentification() }

        onViewReady()
    }

    private fun broadcastIntent(action: String) {
        activity?.run {
            LocalBroadcastManager.getInstance(this)
                    .sendBroadcast(Intent(action))
        }
    }

    override fun onStartVerifyIdentification() {
        broadcastIntent(CoinifySignupActivity.ACTION_NAVIGATE_VERIFY_IDENTIFICATION)
    }

    override fun createPresenter() = presenter

    override fun getMvpView() = this

    companion object {

        @JvmStatic
        fun newInstance(): CoinifyCreateAccountCompletedFragment {
            return CoinifyCreateAccountCompletedFragment()
        }
    }
}