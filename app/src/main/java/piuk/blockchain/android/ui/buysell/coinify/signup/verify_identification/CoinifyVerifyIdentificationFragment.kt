package piuk.blockchain.android.ui.buysell.coinify.signup.verify_identification

import android.content.Intent
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_coinify_verify_identification.*
import piuk.blockchain.android.R
import piuk.blockchain.android.injection.Injector
import piuk.blockchain.android.ui.buysell.coinify.signup.CoinifySignupActivity
import piuk.blockchain.androidcoreui.ui.base.BaseFragment
import piuk.blockchain.androidcoreui.utils.extensions.inflate
import javax.inject.Inject

class CoinifyVerifyIdentificationFragment: BaseFragment<CoinifyVerifyIdentificationView, CoinifyVerifyIdentificationPresenter>(), CoinifyVerifyIdentificationView {

    @Inject
    lateinit var presenterCoinify: CoinifyVerifyIdentificationPresenter

    init {
        Injector.INSTANCE.presenterComponent.inject(this)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ) = container?.inflate(R.layout.fragment_coinify_verify_identification)

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
        broadcastIntent(CoinifySignupActivity.ACTION_NAVIGATE_OVERVIEW)
    }

    override fun createPresenter() = presenterCoinify

    override fun getMvpView() = this

    companion object {

        @JvmStatic
        fun newInstance(): CoinifyVerifyIdentificationFragment {
            return CoinifyVerifyIdentificationFragment()
        }
    }
}