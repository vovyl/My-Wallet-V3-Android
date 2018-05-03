package piuk.blockchain.android.ui.buysell.coinify.signup.verify_email

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_coinify_verify_email.*
import piuk.blockchain.android.R
import piuk.blockchain.android.injection.Injector
import piuk.blockchain.android.ui.buysell.coinify.signup.CoinifyFlowListener
import piuk.blockchain.androidcoreui.ui.base.BaseFragment
import piuk.blockchain.androidcoreui.ui.customviews.ToastCustom
import piuk.blockchain.androidcoreui.utils.extensions.gone
import piuk.blockchain.androidcoreui.utils.extensions.inflate
import piuk.blockchain.androidcoreui.utils.extensions.toast
import piuk.blockchain.androidcoreui.utils.extensions.visible
import timber.log.Timber
import javax.inject.Inject

class CoinifyVerifyEmailFragment :
    BaseFragment<CoinifyVerifyEmailView, CoinifyVerifyEmailPresenter>(), CoinifyVerifyEmailView {

    @Inject
    lateinit var presenter: CoinifyVerifyEmailPresenter
    private var signUpListener: CoinifyFlowListener? = null

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

        verifyIdentificationButton.setOnClickListener {
            presenter.onContinueClicked(arguments?.getString(COUNTRY_CODE) ?: "")
        }

        verifyEmailTermsText.setOnClickListener { openCoinifyTerms() }

        verifyEmailTerms.setOnCheckedChangeListener { _, isChecked ->
            verifyIdentificationButton.isEnabled = isChecked
            presenter.onTermsCheckChanged()
        }

        verifyEmailTerms.isChecked = false
        verifyIdentificationButton.isEnabled = false

        verifyEmailOpenEmail.setOnClickListener {
            val intent = Intent(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_APP_EMAIL)
            startActivity(intent)
        }

        onViewReady()
    }

    override fun onStartSignUpSuccess() {
        signUpListener?.requestStartSignUpSuccess()
    }

    override fun onEnableContinueButton(emailVerified: Boolean) {
        verifyIdentificationButton.isEnabled = emailVerified && verifyEmailTerms.isChecked
    }

    override fun onShowVerifiedEmail(emailAddress: String) {

        verifyEmailTitle.text = getString(R.string.buy_sell_verified_email_title)
        verifyEmailMessage2.text =
                getString(R.string.buy_sell_verified_email_message, getString(R.string.coinify))

        verifiedEmailAddress.text = emailAddress
        verifiedEmailAddress.visible()
        verifyEmailMessage1.gone()
        verifyEmailAddress.gone()
        verifyEmailOpenEmail.gone()
    }

    override fun onShowUnverifiedEmail(emailAddress: String) {

        verifyEmailTitle.text = getString(R.string.buy_sell_unverified_email_title)
        verifyEmailAddress.text = emailAddress

        verifiedEmailAddress.gone()
        verifyEmailAddress.visible()
        verifyEmailOpenEmail.visible()
    }

    override fun onShowErrorAndClose() {
        toast(R.string.unexpected_error, ToastCustom.TYPE_ERROR)
        activity?.finish()
    }

    private fun openCoinifyTerms() {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(COINIFY_TERMS_LINK)))
        } catch (e: ActivityNotFoundException) {
            Timber.e(e)
        }
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

    companion object {

        private const val COINIFY_TERMS_LINK = "https://coinify.com/legal/"
        private const val COUNTRY_CODE = "piuk.blockchain.android.ui.buysell.coinify.signup.verify_email.COUNTRY_CODE"

        @JvmStatic
        fun newInstance(countryCode: String): CoinifyVerifyEmailFragment {

            val bundle = Bundle()
            bundle.putString(COUNTRY_CODE, countryCode)

            return CoinifyVerifyEmailFragment().apply {
                arguments = bundle
            }
        }
    }
}