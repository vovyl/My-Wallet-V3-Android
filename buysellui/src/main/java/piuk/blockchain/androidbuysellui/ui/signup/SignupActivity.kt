package piuk.blockchain.androidbuysellui.ui.signup

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.LocalBroadcastManager
import piuk.blockchain.androidbuysellui.R
import piuk.blockchain.androidbuysellui.injector.BuySellInjector
import piuk.blockchain.androidbuysellui.ui.signup.welcome.WelcomeFragment
import piuk.blockchain.androidcoreui.ui.base.BaseMvpActivity
import timber.log.Timber
import javax.inject.Inject

class SignupActivity: BaseMvpActivity<SignupView, SignupPresenter>(), SignupView {

    @Inject lateinit var presenter: SignupPresenter

    lateinit var welcomeFragment: WelcomeFragment

    init {
        BuySellInjector.getInstance().presenterComponent.inject(this)
    }

    val broadCastReceiver = object : BroadcastReceiver() {
        override fun onReceive(contxt: Context?, intent: Intent?) {

            when (intent?.action) {
                ACTION_NAVIGATE_COUNTRY -> onStartCountrySelect()
                ACTION_NAVIGATE_VERIFY_EMAIL -> onStartVerifyEmail()
                ACTION_NAVIGATE_VERIFY_ID -> onStartVerifyIdentity()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        val filterCountrySelect = IntentFilter(ACTION_NAVIGATE_COUNTRY)
        LocalBroadcastManager.getInstance(this).registerReceiver(broadCastReceiver, filterCountrySelect)

        onViewReady()
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadCastReceiver)
        super.onDestroy()
    }

    override fun onStartWelcome() {
        welcomeFragment = WelcomeFragment.newInstance()
        replaceFragment(welcomeFragment)
    }

    override fun onStartCountrySelect() {
        Timber.d("onStartCountrySelect")
//        addFragmentToBackStack(SelectCountryFragment)
    }

    override fun onStartVerifyEmail() {
        Timber.d("onStartVerifyEmail")
//        addFragmentToBackStack(VerifyEmailFragment)
    }

    override fun onStartVerifyIdentity() {
        Timber.d("onStartVerifyIdentity")
//        addFragmentToBackStack(VerifyIdentificationFragment)
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val transaction = fragmentManager.beginTransaction()
        transaction.replace(R.id.content_frame, fragment, fragment.javaClass.simpleName)
                .commitAllowingStateLoss()
    }

    private fun addFragmentToBackStack(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        fragmentManager.beginTransaction()
                .addToBackStack(fragment.javaClass.name)
                .add(R.id.content_frame, fragment, fragment.javaClass.simpleName)
                .commitAllowingStateLoss()
    }

    private fun addFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        fragmentManager.beginTransaction()
                .add(R.id.content_frame, fragment, fragment.javaClass.simpleName)
                .commitAllowingStateLoss()
    }

    override fun onBackPressed() {
        when (supportFragmentManager.fragments.size) {
            1 -> finish()
            else -> super.onBackPressed()
        }
    }

    override fun createPresenter() = presenter

    override fun getView() = this

    companion object {

        const val ACTION_NAVIGATE_COUNTRY = "piuk.blockchain.androidbuysellui.ui.signup.SignupActivity.ACTION_NAVIGATE_COUNTRY"
        const val ACTION_NAVIGATE_VERIFY_EMAIL = "piuk.blockchain.androidbuysellui.ui.signup.SignupActivity.ACTION_NAVIGATE_VERIFY_EMAIL"
        const val ACTION_NAVIGATE_VERIFY_ID = "piuk.blockchain.androidbuysellui.ui.signup.SignupActivity.ACTION_NAVIGATE_VERIFY_ID"

        @JvmStatic
            fun start (context: Context) {
            val intent = Intent(context, SignupActivity::class.java)
            context.startActivity(intent)
        }
    }
}