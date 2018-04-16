package piuk.blockchain.androidbuysellui.ui.launcher

import android.content.Context
import android.content.Intent
import android.os.Bundle
import piuk.blockchain.androidbuysellui.R
import piuk.blockchain.androidbuysellui.injector.BuySellInjector
import piuk.blockchain.androidbuysellui.ui.signup.SignupActivity
import piuk.blockchain.androidcoreui.ui.base.BaseMvpActivity
import javax.inject.Inject

/**
 * This activity checks the user's current buy sell account status and redirects to specified signup or overview components.
 */
class BuySellLauncherActivity: BaseMvpActivity<BuySellLauncherView, BuySellLauncherPresenter>(), BuySellLauncherView {

    @Inject lateinit var presenter: BuySellLauncherPresenter

    init {
        BuySellInjector.getInstance().presenterComponent.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buysell_launcher)

        onViewReady()
    }

    override fun createPresenter() = presenter

    override fun getView() = this

    override fun onSignup() {
        SignupActivity.start(this)
    }

    override fun onOverview() {
        SignupActivity.start(this)

    }

    companion object {

        @JvmStatic
        fun start (context: Context) {
            val intent = Intent(context, BuySellLauncherActivity::class.java)
            context.startActivity(intent)
        }
    }
}