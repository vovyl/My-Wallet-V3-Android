package piuk.blockchain.android.ui.buysell.confirmation.sell

import piuk.blockchain.android.ui.buysell.createorder.models.SellConfirmationDisplayModel
import piuk.blockchain.androidcoreui.ui.base.View
import java.util.*

interface CoinifySellConfirmationView : View {

    val locale: Locale

    val displayableQuote: SellConfirmationDisplayModel

    fun updateCounter(timeRemaining: String)

    fun showTimeExpiring()

    fun showQuoteExpiredDialog()

    fun displayProgressDialog()

    fun dismissProgressDialog()

    fun showErrorDialog(errorMessage: String)

}
