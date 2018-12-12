package com.blockchain.kycui.email.validation

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.NavHostFragment.findNavController
import com.blockchain.kycui.navhost.KycProgressListener
import com.blockchain.kycui.navhost.models.KycStep
import com.blockchain.kycui.onfidosplash.OnfidoSplashFragment
import com.blockchain.ui.extensions.throttledClicks
import io.reactivex.Observable
import io.reactivex.rxkotlin.Observables
import org.koin.android.ext.android.inject
import piuk.blockchain.androidcore.utils.helperfunctions.unsafeLazy
import piuk.blockchain.androidcoreui.ui.base.BaseMvpFragment
import piuk.blockchain.androidcoreui.ui.customviews.MaterialProgressDialog
import piuk.blockchain.androidcoreui.utils.ParentActivityDelegate
import piuk.blockchain.androidcoreui.utils.ViewUtils
import piuk.blockchain.androidcoreui.utils.extensions.inflate
import piuk.blockchain.kyc.R
import kotlinx.android.synthetic.main.fragment_kyc_email_validation.button_kyc_email_validation_next as buttonNext
import kotlinx.android.synthetic.main.fragment_kyc_email_validation.text_view_email as textViewEmail

class KycEmailValidationFragment :
    BaseMvpFragment<KycEmailValidationView, KycEmailValidationPresenter>(),
    KycEmailValidationView {

    private val presenter: KycEmailValidationPresenter by inject()
    private val progressListener: KycProgressListener by ParentActivityDelegate(this)
    private var progressDialog: MaterialProgressDialog? = null
    private val email by unsafeLazy { arguments!!.getString(ARGUMENT_EMAIL) }

    override val uiStateObservable: Observable<Pair<String, Unit>> by unsafeLazy {
        Observables.combineLatest(
            Observable.just(email),
            buttonNext.throttledClicks()
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = container?.inflate(R.layout.fragment_kyc_email_validation)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        progressListener.setHostTitle(R.string.kyc_email_title)
        progressListener.incrementProgress(KycStep.EmailVerifiedPage)
        textViewEmail.text = email

        onViewReady()
    }

    override fun showProgressDialog() {
        progressDialog = MaterialProgressDialog(activity).apply {
            setOnCancelListener { presenter.onProgressCancelled() }
            setMessage(R.string.kyc_country_selection_please_wait)
            show()
        }
    }

    override fun dismissProgressDialog() {
        progressDialog?.apply { dismiss() }
        progressDialog = null
    }

    override fun continueSignUp() {
        ViewUtils.hideKeyboard(requireActivity())
        val args = OnfidoSplashFragment.bundleArgs(email)
        findNavController(this).apply {
            // Remove email entry and validation pages from back stack as it would be confusing for the user
            popBackStack(R.id.kycEmailEntryFragment, true)
            navigate(R.id.kycCountrySelectionFragment, args)
        }
    }

    override fun displayErrorDialog(message: Int) {
        AlertDialog.Builder(requireContext(), R.style.AlertDialogStyle)
            .setTitle(R.string.app_name)
            .setMessage(message)
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    override fun createPresenter() = presenter

    override fun getMvpView(): KycEmailValidationView = this

    companion object {

        private const val ARGUMENT_EMAIL = "ARGUMENT_EMAIL"

        fun bundleArgs(email: String): Bundle =
            Bundle().apply {
                putString(ARGUMENT_EMAIL, email)
            }
    }
}
