package piuk.blockchain.android.ui.buysell.confirmation.sell

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import kotlinx.android.synthetic.main.toolbar_general.toolbar_general
import piuk.blockchain.android.R
import piuk.blockchain.android.injection.Injector
import piuk.blockchain.android.ui.buysell.createorder.models.SellConfirmationDisplayModel
import piuk.blockchain.androidcore.utils.helperfunctions.consume
import piuk.blockchain.androidcore.utils.helperfunctions.unsafeLazy
import piuk.blockchain.androidcoreui.ui.base.BaseMvpActivity
import piuk.blockchain.androidcoreui.ui.customviews.MaterialProgressDialog
import piuk.blockchain.androidcoreui.utils.extensions.getResolvedColor
import java.util.*
import javax.inject.Inject
import kotlinx.android.synthetic.main.activity_coinify_sell_confirmation.button_confirm_sell as buttonConfirm
import kotlinx.android.synthetic.main.activity_coinify_sell_confirmation.text_view_time_remaining as textViewTime

class CoinifySellConfirmationActivity :
    BaseMvpActivity<CoinifySellConfirmationView, CoinifySellConfirmationPresenter>(),
    CoinifySellConfirmationView {

    @Inject lateinit var presenter: CoinifySellConfirmationPresenter
    override val locale: Locale = Locale.getDefault()
    override val displayableQuote by unsafeLazy { intent.getParcelableExtra(EXTRA_QUOTE) as SellConfirmationDisplayModel }
    private var progressDialog: MaterialProgressDialog? = null

    init {
        Injector.INSTANCE.presenterComponent.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coinify_sell_confirmation)
        setupToolbar(toolbar_general, R.string.buy_sell_confirmation_title_sell)

        buttonConfirm.setOnClickListener {
            //            presenter.onConfirmClicked()
        }
        renderUi()

        onViewReady()
    }

    @SuppressLint("SetTextI18n")
    private fun renderUi() {
        with(displayableQuote) {
            val currencyIn = currencyToReceive.toUpperCase()
            val currencyOut = currencyToSend.toUpperCase()
        }
    }

    override fun updateCounter(timeRemaining: String) {
        textViewTime.text = timeRemaining
    }

    override fun showTimeExpiring() {
        textViewTime.setTextColor(getResolvedColor(R.color.product_red_medium))
    }

    override fun showQuoteExpiredDialog() {
        AlertDialog.Builder(this, R.style.AlertDialogStyle)
                .setTitle(R.string.app_name)
                .setMessage(R.string.buy_sell_confirmation_order_expired)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    setResult(Activity.RESULT_CANCELED)
                    finish()
                }
                .setCancelable(false)
                .show()
    }

    override fun showErrorDialog(errorMessage: String) {
        AlertDialog.Builder(this, R.style.AlertDialogStyle)
                .setTitle(R.string.app_name)
                .setMessage(errorMessage)
                .setPositiveButton(android.R.string.ok, null)
                .show()
    }

    override fun displayProgressDialog() {
        if (!isFinishing) {
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

    override fun onSupportNavigateUp(): Boolean = consume { finish() }

    override fun createPresenter(): CoinifySellConfirmationPresenter = presenter

    override fun getView(): CoinifySellConfirmationView = this

    companion object {

        private const val EXTRA_QUOTE =
                "piuk.blockchain.android.ui.buysell.confirmation.sell.EXTRA_QUOTE"

        // TODO: Probably need to start for result here like CoinifyBuyConfirmationActivity
        fun start(context: Context, displayModel: SellConfirmationDisplayModel) {
            Intent(context, CoinifySellConfirmationActivity::class.java)
                    .apply { putExtra(EXTRA_QUOTE, displayModel) }
                    .run { context.startActivity(this) }
        }

    }
}
