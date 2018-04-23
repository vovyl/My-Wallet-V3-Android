package piuk.blockchain.android.ui.buysell.launcher

import android.content.Context
import android.content.Intent
import android.os.Bundle
import piuk.blockchain.android.R
import piuk.blockchain.android.injection.Injector
import piuk.blockchain.android.ui.buysell.coinify.signup.CoinifySignupActivity
import piuk.blockchain.androidcoreui.ui.base.BaseMvpActivity
import piuk.blockchain.androidcoreui.ui.customviews.ToastCustom
import javax.inject.Inject

/**
 * This activity checks the user's current buy sell account status and redirects to specified signup or overview components.
 */
class BuySellLauncherActivity: BaseMvpActivity<BuySellLauncherView, BuySellLauncherPresenter>(), BuySellLauncherView {

    @Inject lateinit var presenter: BuySellLauncherPresenter

    init {
        Injector.getInstance().presenterComponent.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buysell_launcher)

        onViewReady()
    }

    override fun createPresenter() = presenter

    override fun getView() = this

    override fun onStartCoinifySignup() {
        CoinifySignupActivity.start(this)
        finish()
    }

    override fun onStartCoinifyOverview() {
        ToastCustom.makeText(this, "Coinify Overview coming soon", ToastCustom.LENGTH_SHORT, ToastCustom.TYPE_OK)
        finish()
    }

    companion object {

        @JvmStatic
        fun start (context: Context) {
            val intent = Intent(context, BuySellLauncherActivity::class.java)
            context.startActivity(intent)
        }
    }
}