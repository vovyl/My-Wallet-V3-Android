package piuk.blockchain.android.ui.buysell.payment.bank.addaddress

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import piuk.blockchain.android.R
import piuk.blockchain.android.injection.Injector
import piuk.blockchain.androidcore.utils.helperfunctions.unsafeLazy
import piuk.blockchain.androidcoreui.ui.base.BaseMvpActivity
import piuk.blockchain.androidcoreui.ui.customviews.MaterialProgressDialog
import piuk.blockchain.androidcoreui.ui.customviews.ToastCustom
import piuk.blockchain.androidcoreui.utils.extensions.getTextString
import piuk.blockchain.androidcoreui.utils.extensions.toast
import javax.inject.Inject
import kotlinx.android.synthetic.main.activity_add_address.button_confirm as buttonConfirm
import kotlinx.android.synthetic.main.activity_add_address.edit_text_city as editTextCity
import kotlinx.android.synthetic.main.activity_add_address.edit_text_name as editTextName
import kotlinx.android.synthetic.main.activity_add_address.edit_text_name as editTextStreet
import kotlinx.android.synthetic.main.activity_add_address.edit_text_postcode as editTextPostCode
import kotlinx.android.synthetic.main.activity_add_address.wheel_picker_country as countryPicker
import kotlinx.android.synthetic.main.toolbar_general.toolbar_general as toolBar

class AddAddressActivity : BaseMvpActivity<AddAddressView, AddAddressPresenter>(), AddAddressView {

    @Inject lateinit var presenter: AddAddressPresenter
    override val iban: String by unsafeLazy { intent.getStringExtra(EXTRA_IBAN) }
    override val bic: String by unsafeLazy { intent.getStringExtra(EXTRA_BIC) }
    override val accountHolderName: String
        get() = editTextName.getTextString()
    override val streetAndNumber: String
        get() = editTextStreet.getTextString()
    override val city: String
        get() = editTextCity.getTextString()
    override val postCode: String
        get() = editTextPostCode.getTextString()
    override val countryCodePosition: Int
        get() = countryPicker.currentItemPosition
    private var progressDialog: MaterialProgressDialog? = null

    init {
        Injector.INSTANCE.presenterComponent.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_address)
        setupToolbar(toolBar, R.string.buy_sell_add_account_title)

        buttonConfirm.setOnClickListener { presenter.onConfirmClicked() }

        onViewReady()
    }

    override fun setCountryPickerData(countryList: List<String>) {
        countryPicker.data = countryList
    }

    override fun onAutoSelectCountry(position: Int) {
        countryPicker.selectedItemPosition = position
    }

    override fun showToast(message: Int, toastType: String) {
        toast(message, toastType)
    }

    // TODO: This needs finishing  
    override fun goToConfirmation() {
        toast("Bank account added successfully", ToastCustom.TYPE_OK)
    }

    override fun showErrorDialog(errorDescription: String) {
        AlertDialog.Builder(this, R.style.AlertDialogStyle)
                .setTitle(R.string.app_name)
                .setMessage(errorDescription)
                .setPositiveButton(android.R.string.ok, null)
                .show()
    }

    override fun showProgressDialog() {
        if (!isFinishing) {
            dismissProgressDialog()
            progressDialog = MaterialProgressDialog(this).apply {
                setMessage(getString(R.string.please_wait))
                setCancelable(false)
                show()
            }
        }
    }

    override fun dismissProgressDialog() {
        if (progressDialog?.isShowing == true) {
            progressDialog!!.dismiss()
            progressDialog = null
        }
    }

    override fun createPresenter(): AddAddressPresenter = presenter

    override fun getView(): AddAddressView = this

    companion object {

        private const val EXTRA_IBAN =
                "piuk.blockchain.android.ui.buysell.payment.bank.addaddress.EXTRA_IBAN"
        private const val EXTRA_BIC =
                "piuk.blockchain.android.ui.buysell.payment.bank.addaddress.EXTRA_BIC"

        fun start(context: Context, iban: String, bic: String) {
            Intent(context, AddAddressActivity::class.java)
                    .apply { putExtra(EXTRA_IBAN, iban) }
                    .apply { putExtra(EXTRA_BIC, bic) }
                    .run { context.startActivity(this) }
        }

    }
}