package piuk.blockchain.android.ui.buysell.details

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import kotlinx.android.synthetic.main.toolbar_general.*
import piuk.blockchain.android.R
import piuk.blockchain.android.ui.buysell.details.models.AwaitingFundsModel
import piuk.blockchain.androidcore.utils.helperfunctions.consume
import piuk.blockchain.androidcoreui.ui.base.BaseAuthActivity
import piuk.blockchain.androidcoreui.utils.extensions.toast
import kotlinx.android.synthetic.main.activity_coinify_awaiting_transfer.text_view_awaiting_funds_description as textViewDescription
import kotlinx.android.synthetic.main.activity_coinify_awaiting_transfer.text_view_bank_text as textViewBank
import kotlinx.android.synthetic.main.activity_coinify_awaiting_transfer.text_view_bic_text as textViewBic
import kotlinx.android.synthetic.main.activity_coinify_awaiting_transfer.text_view_copy_all as textViewCopyAll
import kotlinx.android.synthetic.main.activity_coinify_awaiting_transfer.text_view_funds_already_sent as textViewFundsAlreadySent
import kotlinx.android.synthetic.main.activity_coinify_awaiting_transfer.text_view_iban_text as textViewIban
import kotlinx.android.synthetic.main.activity_coinify_awaiting_transfer.text_view_recipient_address_text as textViewRecipientAddress
import kotlinx.android.synthetic.main.activity_coinify_awaiting_transfer.text_view_recipient_name_text as textViewRecipientName
import kotlinx.android.synthetic.main.activity_coinify_awaiting_transfer.text_view_reference_text as textViewReference
import kotlinx.android.synthetic.main.activity_coinify_awaiting_transfer.view_bank_background as viewBank
import kotlinx.android.synthetic.main.activity_coinify_awaiting_transfer.view_bic_background as viewBic
import kotlinx.android.synthetic.main.activity_coinify_awaiting_transfer.view_iban_background as viewIban
import kotlinx.android.synthetic.main.activity_coinify_awaiting_transfer.view_recipient_address_background as viewRecipientAddress
import kotlinx.android.synthetic.main.activity_coinify_awaiting_transfer.view_recipient_name_background as viewRecipientName
import kotlinx.android.synthetic.main.activity_coinify_awaiting_transfer.view_reference_background as viewReference

class CoinifyAwaitingBankTransferActivity : BaseAuthActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coinify_awaiting_transfer)
        setupToolbar(toolbar_general, R.string.buy_sell_state_awaiting_funds)

        // Check Intent for validity
        require(intent.hasExtra(EXTRA_AWAITING_FUNDS_MODEL)) { "Intent does not contain AwaitingFundsModel, please start this Activity via the static factory method start()." }

        renderUi(intent.getParcelableExtra(EXTRA_AWAITING_FUNDS_MODEL))
    }

    private fun renderUi(dataModel: AwaitingFundsModel) {
        textViewDescription.text = getString(
                R.string.buy_sell_awaiting_funds_description,
                dataModel.formattedAmount
        )
        textViewReference.text = dataModel.reference
        textViewRecipientName.text = dataModel.recipientName
        textViewRecipientAddress.text = dataModel.recipientAddress
        textViewIban.text = dataModel.iban
        textViewBic.text = dataModel.bic
        textViewBank.text = dataModel.bank

        textViewFundsAlreadySent.setOnClickListener { showFundsSentDialog() }
        textViewCopyAll.setOnClickListener {
            copyToClipboard(
                    "${dataModel.reference}\n${dataModel.recipientName}\n${dataModel.recipientAddress}\n${dataModel.iban}\n${dataModel.bic}\n${dataModel.bank}\n"
            )
        }

        viewReference.setOnClickListener { copyToClipboard(textViewReference.text.toString()) }
        viewRecipientName.setOnClickListener { copyToClipboard(textViewRecipientName.text.toString()) }
        viewRecipientAddress.setOnClickListener { copyToClipboard(textViewRecipientAddress.text.toString()) }
        viewIban.setOnClickListener { copyToClipboard(textViewIban.text.toString()) }
        viewBic.setOnClickListener { copyToClipboard(textViewBic.text.toString()) }
        viewBank.setOnClickListener { copyToClipboard(textViewBank.text.toString()) }
    }

    private fun copyToClipboard(data: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Bank Data", data)
        toast(R.string.copied_to_clipboard)
        clipboard.primaryClip = clip
    }

    private fun showFundsSentDialog() {
        AlertDialog.Builder(this, R.style.AlertDialogStyle)
                .setTitle(R.string.buy_sell_awaiting_funds_already_sent_title)
                .setMessage(R.string.buy_sell_awaiting_funds_already_sent_message)
                .setPositiveButton(android.R.string.ok, null)
                .show()
    }

    override fun onSupportNavigateUp(): Boolean = consume { onBackPressed() }

    companion object {

        private const val EXTRA_AWAITING_FUNDS_MODEL =
                "piuk.blockchain.android.ui.buysell.details.EXTRA_AWAITING_FUNDS_MODEL"

        internal fun start(context: Context, awaitingFundsModel: AwaitingFundsModel) {
            Intent(context, CoinifyAwaitingBankTransferActivity::class.java).apply {
                putExtra(EXTRA_AWAITING_FUNDS_MODEL, awaitingFundsModel)
            }.run { context.startActivity(this) }
        }

    }

}