package piuk.blockchain.android.ui.buysell.details

import android.content.Context
import android.content.Intent
import android.os.Bundle
import kotlinx.android.synthetic.main.toolbar_general.*
import piuk.blockchain.android.R
import piuk.blockchain.android.ui.buysell.details.models.BuySellDetailsModel
import piuk.blockchain.androidcore.utils.helperfunctions.consume
import piuk.blockchain.androidcoreui.ui.base.BaseAuthActivity
import kotlinx.android.synthetic.main.activity_coinify_transaction_detail.text_view_amount_text as amount
import kotlinx.android.synthetic.main.activity_coinify_transaction_detail.text_view_currency_received_text as currencyReceivedText
import kotlinx.android.synthetic.main.activity_coinify_transaction_detail.text_view_currency_received_title as currencyReceivedTitle
import kotlinx.android.synthetic.main.activity_coinify_transaction_detail.text_view_exchange_rate_text as exchangeRate
import kotlinx.android.synthetic.main.activity_coinify_transaction_detail.text_view_order_amount as orderAmountHeader
import kotlinx.android.synthetic.main.activity_coinify_transaction_detail.text_view_payment_fee_text as paymentFee
import kotlinx.android.synthetic.main.activity_coinify_transaction_detail.text_view_total_text as totalCost
import kotlinx.android.synthetic.main.activity_coinify_transaction_detail.text_view_trade_id_text as tradeId
import kotlinx.android.synthetic.main.activity_coinify_transaction_detail.text_view_transaction_date as dateHeader
import kotlinx.android.synthetic.main.activity_coinify_transaction_detail.text_view_transaction_fee_text as transactionFee

class CoinifyTransactionDetailActivity : BaseAuthActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coinify_transaction_detail)
        // Check Intent for validity
        require(intent.hasExtra(EXTRA_DETAILS_MODEL)) { "Intent does not contain BuySellDetailsModel, please start this Activity via the static factory method start()." }

        renderUi(intent.getParcelableExtra(EXTRA_DETAILS_MODEL))
    }

    private fun renderUi(model: BuySellDetailsModel) {
        setupToolbar(toolbar_general, model.pageTitle)

        orderAmountHeader.text = model.amountReceived
        dateHeader.text = model.date
        tradeId.text = model.tradeId
        transactionFee.text = model.transactionFee
        currencyReceivedTitle.text = model.currencyReceivedTitle
        currencyReceivedText.text = model.amountReceived
        exchangeRate.text = model.exchangeRate
        amount.text = model.amountSent
        paymentFee.text = model.paymentFee
        totalCost.text = model.totalCost
    }

    override fun onSupportNavigateUp(): Boolean = consume { onBackPressed() }

    companion object {

        private const val EXTRA_DETAILS_MODEL =
                "piuk.blockchain.android.ui.buysell.details.EXTRA_DETAILS_MODEL"

        internal fun start(context: Context, buySellDetailsModel: BuySellDetailsModel) {
            Intent(context, CoinifyTransactionDetailActivity::class.java).apply {
                putExtra(EXTRA_DETAILS_MODEL, buySellDetailsModel)
            }.run { context.startActivity(this) }
        }

    }

}