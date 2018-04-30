package piuk.blockchain.android.ui.buysell.overview

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.toolbar_general.*
import piuk.blockchain.android.R
import piuk.blockchain.android.injection.Injector
import piuk.blockchain.android.ui.buysell.overview.adapter.CoinifyOverviewAdapter
import piuk.blockchain.android.ui.buysell.overview.adapter.CoinifyTxFeedListener
import piuk.blockchain.androidcore.utils.helperfunctions.unsafeLazy
import piuk.blockchain.androidcoreui.ui.base.BaseMvpActivity
import piuk.blockchain.androidcoreui.utils.extensions.toast
import javax.inject.Inject
import kotlinx.android.synthetic.main.activity_coinify_overview.recycler_view_coinify_overview as recyclerView
import kotlinx.android.synthetic.main.activity_coinify_overview.swipe_refresh_layout_coinify as swipeRefresh

class CoinifyOverviewActivity : BaseMvpActivity<CoinifyOverviewView, CoinifyOverviewPresenter>(),
    CoinifyOverviewView {

    @Inject lateinit var presenter: CoinifyOverviewPresenter
    private val adapter by unsafeLazy {
        CoinifyOverviewAdapter(
                object : CoinifyTxFeedListener {
                    override fun onTransactionClicked() {

                    }
                }
        )
    }

    init {
        Injector.INSTANCE.presenterComponent.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coinify_overview)
        setupToolbar(toolbar_general, R.string.buy_sell)

        swipeRefresh.setOnRefreshListener { presenter.onViewReady() }

        with(recyclerView) {
            layoutManager = LinearLayoutManager(this@CoinifyOverviewActivity)
            adapter = this@CoinifyOverviewActivity.adapter
        }

        onViewReady()
    }

    override fun updateList(items: List<BuySellDisplayable>) {
        adapter.items = items
    }

    override fun showToast(message: String, toastType: String) {
        toast(message, toastType)
    }

    override fun createPresenter(): CoinifyOverviewPresenter = presenter

    override fun getView(): CoinifyOverviewView = this

    companion object {

        @JvmStatic
        fun start(context: Context) =
                Intent(context, CoinifyOverviewActivity::class.java)
                        .run { context.startActivity(this) }

    }
}