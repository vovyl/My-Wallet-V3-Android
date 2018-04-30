package piuk.blockchain.android.ui.buysell.overview

import piuk.blockchain.androidcoreui.ui.base.View
import piuk.blockchain.androidcoreui.ui.customviews.ToastCustom

interface CoinifyOverviewView : View {

    fun updateList(items: List<BuySellDisplayable>)

    fun showToast(message: String, @ToastCustom.ToastType toastType: String)

}