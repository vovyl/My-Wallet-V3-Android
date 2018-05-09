package piuk.blockchain.android.ui.buysell.payment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import kotlinx.android.synthetic.main.toolbar_general.*
import piuk.blockchain.android.R
import piuk.blockchain.android.injection.Injector
import piuk.blockchain.android.ui.buysell.payment.models.OrderType
import piuk.blockchain.androidcoreui.ui.base.BaseMvpActivity
import javax.inject.Inject

class BuySellBuildOrderActivity :
    BaseMvpActivity<BuySellBuildOrderView, BuySellBuildOrderPresenter>(), BuySellBuildOrderView {

    @Inject lateinit var presenter: BuySellBuildOrderPresenter

    init {
        Injector.INSTANCE.presenterComponent.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buy_sell_build_order)
        require(intent.hasExtra(EXTRA_ORDER_TYPE)) { "You must pass an order type to the Activity. Please start this Activity via the provided static factory method." }

        val orderType = intent.getSerializableExtra(EXTRA_ORDER_TYPE) as OrderType
        val title = when (orderType) {
            OrderType.Buy -> R.string.buy_sell_buy
            OrderType.Sell -> R.string.buy_sell_sell
        }

        setupToolbar(toolbar_general, title)
    }

    override fun createPresenter(): BuySellBuildOrderPresenter = presenter

    override fun getView(): BuySellBuildOrderView = this

    companion object {

        private const val EXTRA_ORDER_TYPE =
                "piuk.blockchain.android.ui.buysell.payment.EXTRA_ORDER_TYPE"

        fun start(context: Context, orderType: OrderType) {
            Intent(context, BuySellBuildOrderActivity::class.java)
                    .apply { putExtra(EXTRA_ORDER_TYPE, orderType) }
                    .run { context.startActivity(this) }
        }

    }

}