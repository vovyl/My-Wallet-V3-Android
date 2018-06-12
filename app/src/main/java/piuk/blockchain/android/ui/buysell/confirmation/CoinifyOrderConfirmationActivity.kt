package piuk.blockchain.android.ui.buysell.confirmation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import piuk.blockchain.android.R
import piuk.blockchain.android.injection.Injector
import piuk.blockchain.android.ui.buysell.createorder.models.ConfirmationDisplay
import piuk.blockchain.android.ui.buysell.createorder.models.OrderType
import piuk.blockchain.androidcore.utils.helperfunctions.unsafeLazy
import piuk.blockchain.androidcoreui.ui.base.BaseMvpActivity
import timber.log.Timber
import javax.inject.Inject
import kotlinx.android.synthetic.main.activity_coinify_confirmation.button_confirm as buttonConfirm
import kotlinx.android.synthetic.main.activity_coinify_confirmation.text_view_btc_to_be_received_detail as textViewToBeReceivedDetail
import kotlinx.android.synthetic.main.activity_coinify_confirmation.text_view_btc_to_be_received_title as textViewToBeReceivedTitle
import kotlinx.android.synthetic.main.activity_coinify_confirmation.text_view_payment_fee_detail as textViewSendFeeDetail
import kotlinx.android.synthetic.main.activity_coinify_confirmation.text_view_receive_amount_detail as textViewReceiveDetail
import kotlinx.android.synthetic.main.activity_coinify_confirmation.text_view_receive_amount_title as textViewReceiveTitle
import kotlinx.android.synthetic.main.activity_coinify_confirmation.text_view_send_amount_detail as textViewSendAmountDetail
import kotlinx.android.synthetic.main.activity_coinify_confirmation.text_view_time_remaining as textViewTimeRemaining
import kotlinx.android.synthetic.main.activity_coinify_confirmation.text_view_total_cost_detail as textViewTotalCostDetail
import kotlinx.android.synthetic.main.activity_coinify_confirmation.text_view_transaction_fee_detail as textViewReceiveFeeDetail
import kotlinx.android.synthetic.main.toolbar_general.toolbar_general as toolbar

class CoinifyOrderConfirmationActivity :
    BaseMvpActivity<CoinifyOrderConfirmationView, CoinifyOrderConfirmationPresenter>(),
    CoinifyOrderConfirmationView {

    @Inject lateinit var presenter: CoinifyOrderConfirmationPresenter
    override val orderType by unsafeLazy { intent.getSerializableExtra(EXTRA_ORDER_TYPE) as OrderType }
    override val confirmationDisplay by unsafeLazy { intent.getSerializableExtra(EXTRA_QUOTE) as ConfirmationDisplay }

    init {
        Injector.INSTANCE.presenterComponent.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coinify_confirmation)

        when (orderType) {
            OrderType.Buy, OrderType.BuyCard -> R.string.buy_sell_confirmation_title_buy
            OrderType.Sell -> R.string.buy_sell_confirmation_title_sell
        }.run { setupToolbar(toolbar, this) }

        Timber.d(confirmationDisplay.toString())

    }

    override fun createPresenter(): CoinifyOrderConfirmationPresenter = presenter

    override fun getView(): CoinifyOrderConfirmationView = this

    companion object {

        private const val EXTRA_ORDER_TYPE =
                "piuk.blockchain.android.ui.buysell.confirmation.EXTRA_ORDER_TYPE"
        private const val EXTRA_QUOTE =
                "piuk.blockchain.android.ui.buysell.confirmation.EXTRA_QUOTE"

        fun start(context: Context, orderType: OrderType, quote: ConfirmationDisplay) {
            Intent(context, CoinifyOrderConfirmationActivity::class.java)
                    .apply { putExtra(EXTRA_ORDER_TYPE, orderType) }
                    .apply { putExtra(EXTRA_QUOTE, quote) }
                    .run { context.startActivity(this) }
        }

    }
}