package piuk.blockchain.android.ui.buysell.payment.bank.addaccount

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import piuk.blockchain.android.R
import piuk.blockchain.android.injection.Injector
import piuk.blockchain.androidcoreui.ui.base.BaseMvpActivity
import piuk.blockchain.androidcoreui.ui.customviews.MaterialProgressDialog
import piuk.blockchain.androidcoreui.ui.customviews.ToastCustom
import piuk.blockchain.androidcoreui.utils.extensions.getTextString
import piuk.blockchain.androidcoreui.utils.extensions.toast
import javax.inject.Inject
import kotlinx.android.synthetic.main.activity_add_bank_account.button_confirm as buttonConfirm
import kotlinx.android.synthetic.main.activity_add_bank_account.edit_text_bic as editTextBic
import kotlinx.android.synthetic.main.activity_add_bank_account.edit_text_iban as editTextIban
import kotlinx.android.synthetic.main.toolbar_general.toolbar_general as toolBar

class AddBankAccountActivity : BaseMvpActivity<AddBankAccountView, AddBankAccountPresenter>(),
    AddBankAccountView {

    @Inject lateinit var presenter: AddBankAccountPresenter
    override val iban: String
        get() = editTextIban.getTextString()
    override val bic: String
        get() = editTextBic.getTextString()
    private var progressDialog: MaterialProgressDialog? = null

    init {
        Injector.INSTANCE.presenterComponent.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_bank_account)
        setupToolbar(toolBar, R.string.buy_sell_add_account_title)

        buttonConfirm.setOnClickListener { presenter.onConfirmClicked() }
    }

    override fun goToConfirmation() {
        toast("Card successfully added", ToastCustom.TYPE_OK)
    }

    override fun showToast(message: Int, toastType: String) {
        toast(message, toastType)
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

    override fun createPresenter(): AddBankAccountPresenter = presenter

    override fun getView(): AddBankAccountView = this

    companion object {

//        private const val EXTRA_ORDER_TYPE =
//                "piuk.blockchain.android.ui.buysell.payment.EXTRA_ORDER_TYPE"
//        private const val REQUEST_CODE_CHOOSE_ACCOUNT = 1001

        fun start(context: Context) {
            Intent(context, AddBankAccountActivity::class.java)
//                    .apply { putExtra(EXTRA_ORDER_TYPE, orderType) }
                    .run { context.startActivity(this) }
        }

    }
}