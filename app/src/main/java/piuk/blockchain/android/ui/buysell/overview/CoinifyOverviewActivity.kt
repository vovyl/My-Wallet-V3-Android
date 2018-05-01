package piuk.blockchain.android.ui.buysell.overview

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.annotation.StringRes
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.toolbar_general.*
import piuk.blockchain.android.R
import piuk.blockchain.android.injection.Injector
import piuk.blockchain.android.ui.buysell.overview.adapter.CoinifyOverviewAdapter
import piuk.blockchain.android.ui.buysell.overview.adapter.CoinifyTxFeedListener
import piuk.blockchain.android.ui.buysell.overview.models.BuySellDisplayable
import piuk.blockchain.androidcore.utils.helperfunctions.consume
import piuk.blockchain.androidcore.utils.helperfunctions.unsafeLazy
import piuk.blockchain.androidcoreui.ui.base.BaseMvpActivity
import piuk.blockchain.androidcoreui.ui.customviews.MaterialProgressDialog
import piuk.blockchain.androidcoreui.ui.customviews.ToastCustom
import piuk.blockchain.androidcoreui.utils.extensions.toast
import javax.inject.Inject
import kotlinx.android.synthetic.main.activity_coinify_overview.recycler_view_coinify_overview as recyclerView
import kotlinx.android.synthetic.main.activity_coinify_overview.swipe_refresh_layout_coinify as swipeRefresh

class CoinifyOverviewActivity : BaseMvpActivity<CoinifyOverviewView, CoinifyOverviewPresenter>(),
    CoinifyOverviewView {

    @Inject lateinit var presenter: CoinifyOverviewPresenter
    private var progressDialog: MaterialProgressDialog? = null
    private val adapter by unsafeLazy {
        CoinifyOverviewAdapter(
                object : CoinifyTxFeedListener {
                    override fun onKycReviewClicked() {
                        launchCardBuyFlow()
                    }

                    override fun onTransactionClicked(transactionId: Int) {
                        toast("Transaction $transactionId clicked")
                    }

                    override fun onBuyClicked() {
                        presenter.onBuySelected()
                    }

                    override fun onSellClicked() {
                        presenter.onSellSelected()
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

        swipeRefresh.setOnRefreshListener { presenter.refreshTransactionList() }

        with(recyclerView) {
            layoutManager = LinearLayoutManager(this@CoinifyOverviewActivity)
            adapter = this@CoinifyOverviewActivity.adapter
        }

        onViewReady()
    }

    override fun renderViewState(state: OverViewState) {
        when (state) {
            is OverViewState.Loading -> swipeRefresh.isRefreshing = true
            is OverViewState.Data -> onData(state.items)
            is OverViewState.Failure -> onError(state.message)
        }
    }

    private fun onData(data: List<BuySellDisplayable>) {
        swipeRefresh.isRefreshing = false
        recyclerView.scrollToPosition(0)
        adapter.items = data
    }

    private fun onError(@StringRes message: Int) {
        swipeRefresh.isRefreshing = false
        toast(message, ToastCustom.TYPE_ERROR)
    }

    override fun launchCardBuyFlow() {
        toast("Not sure how to launch this exact flow yet")
    }

    override fun launchPaymentSelectionFlow() {
        toast("Not sure how to launch this exact flow yet")
    }

    override fun launchSellFlow() {
        toast("Not sure how to launch this exact flow yet")
    }

    override fun showAlertDialog(message: Int) {
        AlertDialog.Builder(this, R.style.AlertDialogStyle)
                .setTitle(R.string.app_name)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null)
                .show()
    }

    override fun displayProgressDialog() {
        dismissProgressDialog()
        if (!isFinishing) {
            progressDialog = MaterialProgressDialog(this).apply {
                setMessage(getString(R.string.please_wait))
                setCancelable(false)
                show()
            }
        }
    }

    override fun dismissProgressDialog() {
        if (progressDialog != null && progressDialog!!.isShowing) {
            progressDialog!!.dismiss()
            progressDialog = null
        }
    }

    override fun onSupportNavigateUp(): Boolean = consume { onBackPressed() }

    override fun createPresenter(): CoinifyOverviewPresenter = presenter

    override fun getView(): CoinifyOverviewView = this

    companion object {

        fun start(context: Context) =
                Intent(context, CoinifyOverviewActivity::class.java)
                        .run { context.startActivity(this) }

    }
}