package piuk.blockchain.android.ui.buysell.coinify.signup.createaccountstart

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_coinify_create_account_start.*
import piuk.blockchain.android.R
import piuk.blockchain.android.injection.Injector
import piuk.blockchain.android.ui.buysell.coinify.signup.CoinifyFlowListener
import piuk.blockchain.androidcoreui.ui.base.BaseFragment
import piuk.blockchain.androidcoreui.utils.extensions.inflate
import javax.inject.Inject

class CoinifyCreateAccountStartFragment :
    BaseFragment<CoinifyCreateAccountStartView, CoinifyCreateAccountStartPresenter>(),
    CoinifyCreateAccountStartView {

    @Inject lateinit var presenter: CoinifyCreateAccountStartPresenter
    private var signUpListener: CoinifyFlowListener? = null

    init {
        Injector.INSTANCE.presenterComponent.inject(this)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ) = container?.inflate(R.layout.fragment_coinify_create_account_start)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        buysellWelcomeButton.setOnClickListener { onStartSelectCountry() }

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

    override fun createPresenter() = presenter

    override fun getMvpView() = this

    override fun onStartSelectCountry() {
        signUpListener?.requestStartSelectCountry()
    }

    companion object {

        @JvmStatic
        fun newInstance(): CoinifyCreateAccountStartFragment {
            return CoinifyCreateAccountStartFragment()
        }
    }
}