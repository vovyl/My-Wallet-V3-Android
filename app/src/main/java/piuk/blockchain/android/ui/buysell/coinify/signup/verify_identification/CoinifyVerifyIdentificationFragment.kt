package piuk.blockchain.android.ui.buysell.coinify.signup.verify_identification

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_coinify_verify_identification.*
import piuk.blockchain.android.R
import piuk.blockchain.android.injection.Injector
import piuk.blockchain.android.ui.buysell.coinify.signup.CoinifyFlowListener
import piuk.blockchain.androidcoreui.ui.base.BaseFragment
import piuk.blockchain.androidcoreui.utils.extensions.inflate
import timber.log.Timber
import javax.inject.Inject

class CoinifyVerifyIdentificationFragment :
    BaseFragment<CoinifyVerifyIdentificationView, CoinifyVerifyIdentificationPresenter>(),
    CoinifyVerifyIdentificationView {

    @Inject
    lateinit var presenterCoinify: CoinifyVerifyIdentificationPresenter
    private var signUpListener: CoinifyFlowListener? = null

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

        // TODO webweb fetch return url?

        verifyIdentificationNextButton.setOnClickListener { onStartOverview() }

        onViewReady()
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is CoinifyFlowListener) {
            signUpListener = context
        } else {
            throw RuntimeException("$context must implement CoinifyFlowListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        signUpListener = null
    }

    override fun onStartOverview() {
        signUpListener?.requestStartOverview()
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