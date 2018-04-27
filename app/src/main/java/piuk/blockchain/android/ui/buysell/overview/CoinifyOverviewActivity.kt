package piuk.blockchain.android.ui.buysell.overview

import android.content.Context
import android.content.Intent
import android.os.Bundle
import kotlinx.android.synthetic.main.toolbar_general.*
import piuk.blockchain.android.R
import piuk.blockchain.android.injection.Injector
import piuk.blockchain.androidcoreui.ui.base.BaseMvpActivity
import javax.inject.Inject
import kotlinx.android.synthetic.main.activity_coinify_overview.recycler_view_coinify_overview as recyclerView

class CoinifyOverviewActivity : BaseMvpActivity<CoinifyOverviewView, CoinifyOverviewPresenter>(),
    CoinifyOverviewView {

    @Inject lateinit var presenter: CoinifyOverviewPresenter

    init {
        Injector.INSTANCE.presenterComponent.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coinify_overview)
        setupToolbar(toolbar_general, R.string.buy_sell)
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