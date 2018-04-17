package piuk.blockchain.androidbuysellui.ui.signup

import android.animation.ObjectAnimator
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.content.LocalBroadcastManager
import android.view.animation.DecelerateInterpolator
import kotlinx.android.synthetic.main.activity_signup.*
import kotlinx.android.synthetic.main.include_signup_progress.*
import kotlinx.android.synthetic.main.toolbar_general.*
import piuk.blockchain.androidbuysellui.R
import piuk.blockchain.androidbuysellui.injector.BuySellInjector
import piuk.blockchain.androidbuysellui.ui.signup.create_account_completed.CreateAccountCompletedFragment
import piuk.blockchain.androidbuysellui.ui.signup.create_account_start.CreateAccountStartFragment
import piuk.blockchain.androidbuysellui.ui.signup.select_country.SelectCountryFragment
import piuk.blockchain.androidbuysellui.ui.signup.verify_email.VerifyEmailFragment
import piuk.blockchain.androidbuysellui.ui.signup.verify_identification.VerifyIdentificationFragment
import piuk.blockchain.androidcoreui.ui.base.BaseMvpActivity
import piuk.blockchain.androidcoreui.utils.extensions.gone
import piuk.blockchain.androidcoreui.utils.extensions.visible
import javax.inject.Inject

class SignupActivity: BaseMvpActivity<SignupView, SignupPresenter>(), SignupView,
        FragmentManager.OnBackStackChangedListener {

    @Inject lateinit var presenter: SignupPresenter


    init {
        BuySellInjector.getInstance().presenterComponent.inject(this)
    }

    val broadCastReceiver = object : BroadcastReceiver() {
        override fun onReceive(contxt: Context?, intent: Intent?) {

            when (intent?.action) {
                ACTION_NAVIGATE_SELECT_COUNTRY -> onStartSelectCountry()
                ACTION_NAVIGATE_VERIFY_EMAIL -> onStartVerifyEmail()
                ACTION_NAVIGATE_CREATE_ACCOUNT_COMPLETED -> onStartCreateAccountCompleted()
                ACTION_NAVIGATE_VERIFY_IDENTIFICATION -> onStartVerifyIdentification()
                ACTION_NAVIGATE_OVERVIEW -> onStartOverview()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        setupToolbar(R.string.buy_sell)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        registerNavigationBroadcasts()

        supportFragmentManager.addOnBackStackChangedListener(this)

        buysellSignupProgressBar.max = 100 * 10

        onViewReady()
    }

    private fun setupToolbar(title: Int) {
        setupToolbar(toolbar_general, getString(title))
    }

    private fun registerNavigationBroadcasts() {
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(broadCastReceiver, IntentFilter(ACTION_NAVIGATE_SELECT_COUNTRY))
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(broadCastReceiver, IntentFilter(ACTION_NAVIGATE_VERIFY_EMAIL))
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(broadCastReceiver, IntentFilter(ACTION_NAVIGATE_CREATE_ACCOUNT_COMPLETED))
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(broadCastReceiver, IntentFilter(ACTION_NAVIGATE_VERIFY_IDENTIFICATION))
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(broadCastReceiver, IntentFilter(ACTION_NAVIGATE_OVERVIEW))
    }

    override fun onBackStackChanged() {

        var currentFragment = supportFragmentManager.findFragmentByTag(CURRENT_FRAGMENT_TAG)
        val title = when (currentFragment) {
            is CreateAccountStartFragment -> R.string.buy_sell
            is SelectCountryFragment -> R.string.buy_sell_create_account
            is VerifyEmailFragment -> R.string.buy_sell_create_account
            is CreateAccountCompletedFragment -> R.string.buy_sell_identification_verification
            is VerifyIdentificationFragment -> R.string.buy_sell_identification_verification
            else -> R.string.buy_sell
        }

        setupToolbar(title)
        onProgressUpdate(currentFragment)
    }

    /**
     * This will change due to design change. Plus, this is ugly :)
     */
    override fun onProgressUpdate(currentFragment: Fragment) {
        var progress = when (currentFragment) {
            is CreateAccountStartFragment -> {
                signupProgressLayout.gone()
                0
            }
            is SelectCountryFragment -> {
                signupProgressLayout.visible()
                25
            }
            is VerifyEmailFragment -> {
                signupProgressLayout.visible()
                25
            }
            is CreateAccountCompletedFragment -> {
                signupProgressLayout.visible()
                25
            }
            is VerifyIdentificationFragment -> {
                signupProgressLayout.visible()
                75
            }
            else -> {
                signupProgressLayout.gone()
                0
            }
        }

        ObjectAnimator.ofInt(
                buysellSignupProgressBar,
                "progress",
                buysellSignupProgressBar.progress,
                progress * 10
        ).apply {
            duration = 300
            interpolator = DecelerateInterpolator()
            start()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadCastReceiver)
        super.onDestroy()
    }

    override fun onStartWelcome() {
        replaceFragment(CreateAccountStartFragment.newInstance())
    }

    override fun onStartSelectCountry() {
        addFragmentToBackStack(SelectCountryFragment.newInstance())
    }

    override fun onStartVerifyEmail() {
        addFragmentToBackStack(VerifyEmailFragment.newInstance())
    }

    override fun onStartCreateAccountCompleted() {
        replaceFragment(CreateAccountCompletedFragment.newInstance())
    }

    override fun onStartVerifyIdentification() {
        addFragmentToBackStack(VerifyIdentificationFragment.newInstance())
    }

    override fun onStartOverview() {
        // Start OverviewActivity here
        finish()
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val transaction = fragmentManager.beginTransaction()
        transaction.replace(R.id.content_frame, fragment, CURRENT_FRAGMENT_TAG)
                .commitAllowingStateLoss()
    }

    private fun addFragmentToBackStack(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        fragmentManager.beginTransaction()
                .addToBackStack(fragment.javaClass.name)
                .add(R.id.content_frame, fragment, CURRENT_FRAGMENT_TAG)
                .commitAllowingStateLoss()
    }

    private fun addFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        fragmentManager.beginTransaction()
                .add(R.id.content_frame, fragment, CURRENT_FRAGMENT_TAG)
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

        const val ACTION_NAVIGATE_SELECT_COUNTRY = "piuk.blockchain.androidbuysellui.ui.signup.SignupActivity.ACTION_NAVIGATE_SELECT_COUNTRY"
        const val ACTION_NAVIGATE_VERIFY_EMAIL = "piuk.blockchain.androidbuysellui.ui.signup.SignupActivity.ACTION_NAVIGATE_VERIFY_EMAIL"
        const val ACTION_NAVIGATE_CREATE_ACCOUNT_COMPLETED = "piuk.blockchain.androidbuysellui.ui.signup.SignupActivity.ACTION_NAVIGATE_CREATE_ACCOUNT_COMPLETED"
        const val ACTION_NAVIGATE_VERIFY_IDENTIFICATION = "piuk.blockchain.androidbuysellui.ui.signup.SignupActivity.ACTION_NAVIGATE_VERIFY_IDENTIFICATION"
        const val ACTION_NAVIGATE_OVERVIEW = "piuk.blockchain.androidbuysellui.ui.signup.SignupActivity.ACTION_NAVIGATE_OVERVIEW"

        private const val CURRENT_FRAGMENT_TAG = "piuk.blockchain.androidbuysellui.ui.signup.SignupActivity.CURRENT_FRAGMENT_TAG"

        @JvmStatic
            fun start (context: Context) {
            val intent = Intent(context, SignupActivity::class.java)
            context.startActivity(intent)
        }
    }
}