package piuk.blockchain.android.ui.buysell.confirmation

import piuk.blockchain.android.ui.buysell.createorder.models.ConfirmationDisplay
import piuk.blockchain.android.ui.buysell.createorder.models.OrderType
import piuk.blockchain.androidcoreui.ui.base.View

interface CoinifyOrderConfirmationView : View {

    val orderType: OrderType

    val displayableQuote: ConfirmationDisplay

    fun updateCounter(timeRemaining: String)

    fun showTimeExpiring()

    fun showQuoteExpiredDialog()

    fun displayProgressDialog()

    fun dismissProgressDialog()

    fun launchCardPaymentWebView(redirectUrl: String)
}