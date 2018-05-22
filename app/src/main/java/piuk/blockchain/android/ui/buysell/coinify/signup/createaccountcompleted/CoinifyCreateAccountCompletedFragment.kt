package piuk.blockchain.android.ui.buysell.coinify.signup.createaccountcompleted

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_coinify_create_account_completed.*
import piuk.blockchain.android.R
import piuk.blockchain.android.injection.Injector
import piuk.blockchain.android.ui.buysell.coinify.signup.CoinifyFlowListener
import piuk.blockchain.androidcoreui.ui.base.BaseFragment
import piuk.blockchain.androidcoreui.utils.extensions.inflate
import javax.inject.Inject

class CoinifyCreateAccountCompletedFragment :
    BaseFragment<CoinifyCreateAccountCompletedView, CoinifyCreateAccountCompletedPresenter>(),
    CoinifyCreateAccountCompletedView {

    @Inject
    lateinit var presenter: CoinifyCreateAccountCompletedPresenter
    private var signUpListener: CoinifyFlowListener? = null

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

    override fun onStartVerifyIdentification() {
        signUpListener?.requestStartVerifyIdentification()
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