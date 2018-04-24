package piuk.blockchain.android.ui.buysell.coinify.signup

import android.animation.ObjectAnimator
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.content.ContextCompat
import android.support.v4.content.LocalBroadcastManager
import android.view.animation.DecelerateInterpolator
import kotlinx.android.synthetic.main.activity_coinify_signup.*
import kotlinx.android.synthetic.main.include_buysell_signup_progress.*
import kotlinx.android.synthetic.main.toolbar_general.*
import piuk.blockchain.android.R
import piuk.blockchain.android.injection.Injector
import piuk.blockchain.android.ui.buysell.coinify.signup.create_account_completed.CoinifyCreateAccountCompletedFragment
import piuk.blockchain.android.ui.buysell.coinify.signup.create_account_start.CoinifyCreateAccountStartFragment
import piuk.blockchain.android.ui.buysell.coinify.signup.invalid_country.CoinifyInvalidCountryFragment
import piuk.blockchain.android.ui.buysell.coinify.signup.select_country.CoinifySelectCountryFragment
import piuk.blockchain.android.ui.buysell.coinify.signup.verify_email.CoinifyVerifyEmailFragment
import piuk.blockchain.android.ui.buysell.coinify.signup.verify_identification.CoinifyVerifyIdentificationFragment
import piuk.blockchain.androidcoreui.ui.base.BaseMvpActivity
import piuk.blockchain.androidcoreui.utils.extensions.gone
import piuk.blockchain.androidcoreui.utils.extensions.visible
import javax.inject.Inject

class CoinifySignupActivity: BaseMvpActivity<CoinifySignupView, CoinifySignupPresenter>(), CoinifySignupView,
        FragmentManager.OnBackStackChangedListener {

    @Inject lateinit var presenter: CoinifySignupPresenter


    init {
        Injector.getInstance().presenterComponent.inject(this)
    }

    val broadCastReceiver = object : BroadcastReceiver() {
        override fun onReceive(contxt: Context?, intent: Intent?) {

            when (intent?.action) {
                ACTION_NAVIGATE_SELECT_COUNTRY -> onStartSelectCountry()
                ACTION_NAVIGATE_VERIFY_EMAIL -> {
                    //TODO Collect country code for signup later
                    intent.getStringExtra(CoinifySelectCountryFragment.COUNTRY_CODE)
                    onStartVerifyEmail()
                }
                ACTION_NAVIGATE_CREATE_ACCOUNT_COMPLETED -> onStartCreateAccountCompleted()
                ACTION_NAVIGATE_VERIFY_IDENTIFICATION -> onStartVerifyIdentification()
                ACTION_NAVIGATE_OVERVIEW -> onStartOverview()
                ACTION_NAVIGATE_INVALID_COUNTRY -> onStartInvalidCountry()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coinify_signup)

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
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(broadCastReceiver, IntentFilter(ACTION_NAVIGATE_INVALID_COUNTRY))
    }

    override fun onBackStackChanged() {

        var currentFragment = supportFragmentManager.findFragmentByTag(CURRENT_FRAGMENT_TAG)
        val title = when (currentFragment) {
            is CoinifyCreateAccountStartFragment -> R.string.buy_sell
            is CoinifySelectCountryFragment -> R.string.buy_sell_create_account
            is CoinifyVerifyEmailFragment -> R.string.buy_sell_create_account
            is CoinifyCreateAccountCompletedFragment -> R.string.buy_sell_identification_verification
            is CoinifyVerifyIdentificationFragment -> R.string.buy_sell_identification_verification
            else -> R.string.buy_sell
        }

        setupToolbar(title)
        onProgressUpdate(currentFragment)
    }

    /**
     * This will change due to design change. Plus, this is ugly :)
     */
    override fun onProgressUpdate(currentFragment: Fragment) {
        when (currentFragment) {
            is CoinifyCreateAccountStartFragment -> {
                progressBar(0)
            }
            is CoinifySelectCountryFragment -> {
                progressBar(1)
            }
            is CoinifyVerifyEmailFragment -> {
                progressBar(50)
            }
            is CoinifyCreateAccountCompletedFragment -> {
                progressBar(50)
            }
            is CoinifyVerifyIdentificationFragment -> {
                progressBar(100)
            }
            else -> {
                progressBar(1)
            }
        }
    }

    private fun progressBar(progress: Int) {

        var icon1Color: Int
        var icon2Color: Int
        var icon3Color: Int

        when (progress) {
            in 1 .. 49 -> {
                signupProgressLayout.visible()
                icon1Color = R.color.primary_blue_accent
                icon2Color = R.color.primary_gray_light
                icon3Color = R.color.primary_gray_light
            }
            in 50 .. 99 -> {
                signupProgressLayout.visible()
                icon1Color = R.color.primary_blue_accent
                icon2Color = R.color.primary_blue_accent
                icon3Color = R.color.primary_gray_light
            }
            100 -> {
                signupProgressLayout.visible()
                icon1Color = R.color.primary_blue_accent
                icon2Color = R.color.primary_blue_accent
                icon3Color = R.color.primary_blue_accent
            }
            else -> {
                signupProgressLayout.gone()
                icon1Color = R.color.primary_gray_light
                icon2Color = R.color.primary_gray_light
                icon3Color = R.color.primary_gray_light
            }
        }

        imageView1.setColorFilter(ContextCompat.getColor(this, icon1Color));
        imageView2.setColorFilter(ContextCompat.getColor(this, icon2Color));
        imageView3.setColorFilter(ContextCompat.getColor(this, icon3Color));

        animateProgressBar(progress)
    }

    private fun animateProgressBar(progress: Int) {
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
        replaceFragment(CoinifyCreateAccountStartFragment.newInstance())
    }

    override fun onStartSelectCountry() {
        addFragmentToBackStack(CoinifySelectCountryFragment.newInstance())
    }

    override fun onStartVerifyEmail() {
        addFragmentToBackStack(CoinifyVerifyEmailFragment.newInstance())
    }

    override fun onStartCreateAccountCompleted() {
        replaceFragment(CoinifyCreateAccountCompletedFragment.newInstance())
    }

    override fun onStartVerifyIdentification() {
        addFragmentToBackStack(CoinifyVerifyIdentificationFragment.newInstance())
    }

    override fun onStartOverview() {
        // Start OverviewActivity here
        finish()
    }

    override fun onStartInvalidCountry() {
        replaceFragment(CoinifyInvalidCountryFragment.newInstance())
        progressBar(0)
        setupToolbar(R.string.buy_sell)
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

        const val ACTION_NAVIGATE_SELECT_COUNTRY = "piuk.blockchain.android.ui.buysell.coinify.signup.CoinifySignupActivity.ACTION_NAVIGATE_SELECT_COUNTRY"
        const val ACTION_NAVIGATE_VERIFY_EMAIL = "piuk.blockchain.android.ui.buysell.coinify.signup.CoinifySignupActivity.ACTION_NAVIGATE_VERIFY_EMAIL"
        const val ACTION_NAVIGATE_CREATE_ACCOUNT_COMPLETED = "piuk.blockchain.android.ui.buysell.coinify.signup.CoinifySignupActivity.ACTION_NAVIGATE_CREATE_ACCOUNT_COMPLETED"
        const val ACTION_NAVIGATE_VERIFY_IDENTIFICATION = "piuk.blockchain.android.ui.buysell.coinify.signup.CoinifySignupActivity.ACTION_NAVIGATE_VERIFY_IDENTIFICATION"
        const val ACTION_NAVIGATE_OVERVIEW = "piuk.blockchain.android.ui.buysell.coinify.signup.CoinifySignupActivity.ACTION_NAVIGATE_OVERVIEW"
        const val ACTION_NAVIGATE_INVALID_COUNTRY = "piuk.blockchain.android.ui.buysell.coinify.signup.CoinifySignupActivity.ACTION_NAVIGATE_INVALID_COUNTRY"

        private const val CURRENT_FRAGMENT_TAG = "piuk.blockchain.android.ui.buysell.coinify.signup.CoinifySignupActivity.CURRENT_FRAGMENT_TAG"

        @JvmStatic
            fun start (context: Context) {
            val intent = Intent(context, CoinifySignupActivity::class.java)
            context.startActivity(intent)
        }
    }
}