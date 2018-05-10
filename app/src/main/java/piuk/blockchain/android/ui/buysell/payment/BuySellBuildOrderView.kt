package piuk.blockchain.android.ui.buysell.payment

import piuk.blockchain.androidcoreui.ui.base.View
import java.util.*

interface BuySellBuildOrderView : View {

    val locale: Locale

    fun renderQuoteStatus(status: BuySellBuildOrderPresenter.QuoteStatus)

    fun renderSpinnerStatus(status: BuySellBuildOrderPresenter.SpinnerStatus)

}