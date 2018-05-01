package piuk.blockchain.android.ui.buysell.overview

import android.support.annotation.StringRes
import piuk.blockchain.androidcoreui.ui.base.View

interface CoinifyOverviewView : View {

    fun renderViewState(state: OverViewState)

    fun launchPaymentSelectionFlow()

    fun launchCardBuyFlow()

    fun launchSellFlow()

    fun showAlertDialog(@StringRes message: Int)

    fun displayProgressDialog()

    fun dismissProgressDialog()
}