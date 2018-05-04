package piuk.blockchain.android.ui.buysell.overview

import android.support.annotation.StringRes
import piuk.blockchain.android.ui.buysell.details.models.AwaitingFundsModel
import piuk.blockchain.android.ui.buysell.details.models.BuySellDetailsModel
import piuk.blockchain.androidcoreui.ui.base.View
import java.util.*

interface CoinifyOverviewView : View {

    val locale: Locale

    fun renderViewState(state: OverViewState)

    fun launchPaymentSelectionFlow()

    fun launchCardBuyFlow()

    fun launchSellFlow()

    fun showAlertDialog(@StringRes message: Int)

    fun displayProgressDialog()

    fun dismissProgressDialog()

    fun launchDetailsPage(dataModel: BuySellDetailsModel)

    fun launchAwaitingTransferPage(dataModel: AwaitingFundsModel)
}