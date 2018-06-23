package piuk.blockchain.android.ui.buysell.payment.card

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import piuk.blockchain.android.R
import piuk.blockchain.androidcore.utils.annotations.Thunk
import piuk.blockchain.androidcore.utils.helperfunctions.consume
import piuk.blockchain.androidcore.utils.helperfunctions.unsafeLazy
import piuk.blockchain.androidcoreui.ui.base.BaseAuthActivity
import kotlinx.android.synthetic.main.activity_isignthis_payment.web_view_isignthis as webView
import kotlinx.android.synthetic.main.toolbar_general.toolbar_general as toolBar

class ISignThisActivity : BaseAuthActivity() {

    private val redirectUrl by unsafeLazy { intent.getStringExtra(EXTRA_REDIRECT_URL) }
    private var paymentComplete = false

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_isignthis_payment)
        setupToolbar(toolBar, R.string.buy_sell_isignthis_payment_method)

        webView.settings.javaScriptEnabled = true
        webView.webViewClient = object : WebViewClient() {
            override fun onLoadResource(view: WebView?, url: String?) {
                super.onLoadResource(view, url)
                handleUrl(url)
            }
        }

        webView.webChromeClient = WebChromeClient()
        webView.loadUrl(redirectUrl)
    }

    @Thunk
    fun handleUrl(url: String?) {
        if (url?.contains(TRADE_COMPLETE_PARTIAL_URL) == true) {
            if (!paymentComplete) {
                paymentComplete = true
                val uri = Uri.parse(url)
                val stateString = uri.getQueryParameter("state")
                val state = PaymentState.valueOf(stateString!!)
                launchPaymentCompletePage(state)
            }
        }
    }

    private fun launchPaymentCompletePage(paymentState: PaymentState) {
        CardPaymentCompleteActivity.starter(this, paymentState)
        finish()
    }

    override fun onSupportNavigateUp(): Boolean = consume { onBackPressed() }

    override fun startLogoutTimer() = Unit

    companion object {

        private const val EXTRA_REDIRECT_URL =
                "piuk.blockchain.android.ui.buysell.payment.card.EXTRA_REDIRECT_URL"

        private const val TRADE_COMPLETE_PARTIAL_URL = "https://www.coinify.com/trade/"

        // TODO: Pass paymentId from CardDetails object
        fun start(activity: Activity, redirectUrl: String) {
            Intent(activity, ISignThisActivity::class.java).apply {
                putExtra(EXTRA_REDIRECT_URL, redirectUrl)
            }.run { activity.startActivity(this) }
        }

    }
}