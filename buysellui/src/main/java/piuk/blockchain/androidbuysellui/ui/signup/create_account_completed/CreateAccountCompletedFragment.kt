package piuk.blockchain.androidbuysellui.ui.signup.create_account_completed

import android.content.Intent
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_create_account_completed.*
import piuk.blockchain.androidbuysellui.R
import piuk.blockchain.androidbuysellui.injector.BuySellInjector
import piuk.blockchain.androidbuysellui.ui.signup.SignupActivity
import piuk.blockchain.androidcoreui.ui.base.BaseFragment
import piuk.blockchain.androidcoreui.utils.extensions.inflate
import javax.inject.Inject

class CreateAccountCompletedFragment: BaseFragment<CreateAccountCompletedView, CreateAccountCompletedPresenter>(), CreateAccountCompletedView {

    @Inject
    lateinit var presenter: CreateAccountCompletedPresenter

    init {
        BuySellInjector.INSTANCE.presenterComponent.inject(this)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ) = container?.inflate(R.layout.fragment_create_account_completed)

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
        broadcastIntent(SignupActivity.ACTION_NAVIGATE_VERIFY_IDENTIFICATION)
    }

    override fun createPresenter() = presenter

    override fun getMvpView() = this

    companion object {

        @JvmStatic
        fun newInstance(): CreateAccountCompletedFragment {
            return CreateAccountCompletedFragment()
        }
    }
}