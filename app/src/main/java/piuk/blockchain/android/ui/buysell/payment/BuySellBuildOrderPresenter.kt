package piuk.blockchain.android.ui.buysell.payment

import piuk.blockchain.android.data.payments.SendDataManager
import piuk.blockchain.androidbuysell.datamanagers.CoinifyDataManager
import piuk.blockchain.androidcoreui.ui.base.BasePresenter
import javax.inject.Inject

class BuySellBuildOrderPresenter @Inject constructor(
        private val coinifyDataManager: CoinifyDataManager,
        private val sendDataManager: SendDataManager
) : BasePresenter<BuySellBuildOrderView>() {

    override fun onViewReady() {

    }
}