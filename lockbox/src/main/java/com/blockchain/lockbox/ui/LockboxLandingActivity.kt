package com.blockchain.lockbox.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.constraint.Group
import android.support.v7.widget.Toolbar
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.widget.Button
import android.widget.TextView
import com.blockchain.lockbox.R
import org.koin.android.ext.android.inject
import piuk.blockchain.androidcore.utils.helperfunctions.consume
import piuk.blockchain.androidcoreui.ui.base.BaseMvpActivity
import piuk.blockchain.androidcoreui.utils.extensions.getResolvedColor
import piuk.blockchain.androidcoreui.utils.extensions.goneIf
import piuk.blockchain.androidcoreui.utils.extensions.toast

class LockboxLandingActivity : BaseMvpActivity<LockboxLandingView, LockboxLandingPresenter>(),
    LockboxLandingView {

    private val presenter: LockboxLandingPresenter by inject()

    private lateinit var toolbar: Toolbar
    private lateinit var loading: Group
    private lateinit var noLockbox: Group
    private lateinit var lockboxPaired: Group
    private lateinit var buttonBuyNow: Button
    private lateinit var learnMoreNotPaired: TextView
    private lateinit var learnMorePaired: TextView
    private lateinit var buttonCheckBalance: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lockbox_landing)
        toolbar = findViewById(R.id.toolbar_general)
        loading = findViewById(R.id.group_loading)
        noLockbox = findViewById(R.id.group_no_lockbox)
        lockboxPaired = findViewById(R.id.group_lockpox_paired)
        buttonBuyNow = findViewById(R.id.button_get_lockbox)
        learnMoreNotPaired = findViewById(R.id.text_view_learn_more_not_paired)
        learnMorePaired = findViewById(R.id.text_view_learn_more_paired)
        buttonCheckBalance = findViewById(R.id.button_check_balance)
        setupToolbar(toolbar, R.string.lockbox_title)

        buttonBuyNow.setOnClickListener { launchLockboxSite() }
        learnMoreNotPaired.setOnClickListener { launchLockboxSite() }
        learnMorePaired.setOnClickListener { launchLockboxSite() }
        buttonCheckBalance.setOnClickListener { launchWallet() }

        val linkString = getString(R.string.lockbox_landing_learn_more, LOCKBOX_LEARN_MORE)
        SpannableString(linkString).apply {
            setSpan(
                ForegroundColorSpan(getResolvedColor(R.color.primary_blue_accent)),
                linkString.length - LOCKBOX_LEARN_MORE.length,
                linkString.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }.run {
            learnMoreNotPaired.text = this
            learnMorePaired.text = this
        }

        onViewReady()
    }

    private fun launchLockboxSite() {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(presenter.getComRootLink() + "/lockbox")
            )
        )
    }

    private fun launchWallet() {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(presenter.getWalletLink())
            )
        )
    }

    override fun renderUiState(uiState: LockboxUiState) {
        if (uiState is LockboxUiState.Error) {
            toast(R.string.unexpected_error)
            finish()
            return
        }

        loading.goneIf { uiState !is LockboxUiState.Loading }
        noLockbox.goneIf { uiState !is LockboxUiState.NoLockbox }
        lockboxPaired.goneIf { uiState !is LockboxUiState.LockboxPaired }
    }

    override fun onSupportNavigateUp(): Boolean = consume { finish() }

    override fun createPresenter(): LockboxLandingPresenter = presenter

    override fun getView(): LockboxLandingView = this

    companion object {

        private const val LOCKBOX_LEARN_MORE = "blockchain.com/lockbox"

        @JvmStatic
        fun start(context: Context) {
            Intent(context, LockboxLandingActivity::class.java)
                .run { context.startActivity(this) }
        }
    }
}
