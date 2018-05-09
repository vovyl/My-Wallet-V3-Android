package piuk.blockchain.android.ui.buysell.payment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.toolbar_general.*
import piuk.blockchain.android.R
import piuk.blockchain.android.injection.Injector
import piuk.blockchain.android.ui.buysell.payment.models.OrderType
import piuk.blockchain.androidcore.utils.helperfunctions.consume
import piuk.blockchain.androidcoreui.ui.base.BaseMvpActivity
import piuk.blockchain.androidcoreui.ui.customviews.ToastCustom
import piuk.blockchain.androidcoreui.utils.extensions.gone
import piuk.blockchain.androidcoreui.utils.extensions.invisible
import piuk.blockchain.androidcoreui.utils.extensions.toast
import piuk.blockchain.androidcoreui.utils.extensions.visible
import piuk.blockchain.androidcoreui.utils.helperfunctions.onItemSelectedListener
import java.util.*
import javax.inject.Inject
import kotlinx.android.synthetic.main.activity_buy_sell_build_order.progress_bar_quote_rate as progressBarQuote
import kotlinx.android.synthetic.main.activity_buy_sell_build_order.spinner_currency_selection as spinnerCurrencySelection
import kotlinx.android.synthetic.main.activity_buy_sell_build_order.text_view_quote_price as textViewQuotePrice


class BuySellBuildOrderActivity :
    BaseMvpActivity<BuySellBuildOrderView, BuySellBuildOrderPresenter>(), BuySellBuildOrderView {

    @Inject lateinit var presenter: BuySellBuildOrderPresenter
    override val locale: Locale = Locale.getDefault()

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

        onViewReady()
    }

    override fun renderQuoteStatus(status: BuySellBuildOrderPresenter.QuoteStatus) = when (status) {
        is BuySellBuildOrderPresenter.QuoteStatus.Data -> renderQuoteData(status)
        BuySellBuildOrderPresenter.QuoteStatus.Loading -> renderQuoteLoading()
        BuySellBuildOrderPresenter.QuoteStatus.Failed -> renderQuoteFailure()
    }

    private fun renderQuoteFailure() {
        textViewQuotePrice.invisible()
        progressBarQuote.gone()
        toast(R.string.buy_sell_error_fetching_quote, ToastCustom.TYPE_ERROR)
    }

    private fun renderQuoteData(status: BuySellBuildOrderPresenter.QuoteStatus.Data) {
        textViewQuotePrice.text = status.formattedQuote
        textViewQuotePrice.visible()
        progressBarQuote.gone()
    }

    private fun renderQuoteLoading() {
        textViewQuotePrice.invisible()
        progressBarQuote.visible()
    }

    // TODO: Show spinner loading
    override fun setupSpinner(currencies: List<String>) {
        val dataAdapter = ArrayAdapter<String>(this, R.layout.item_spinner_buy_sell, currencies)
                .apply { setDropDownViewResource(R.layout.item_spinner_buy_sell_list) }

        with(spinnerCurrencySelection) {
            adapter = dataAdapter
            setSelection(0, false)
            onItemSelectedListener = onItemSelectedListener {
                presenter.onCurrencySelected(currencies[it])
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean = consume { onBackPressed() }

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