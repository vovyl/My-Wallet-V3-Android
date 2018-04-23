package piuk.blockchain.android.ui.buysell.coinify.signup.invalid_country

import piuk.blockchain.androidcoreui.ui.base.BasePresenter
import javax.inject.Inject

class CoinifyInvalidCountryPresenter @Inject constructor(

) : BasePresenter<CoinifyInvalidCountryView>() {
    override fun onViewReady() {

    }

    fun requestEmailOnBuySellAvailability() {
        //TODO The current google doc is not accepting further responses - Due to time constraint we'll come back to this later
        view.close()
    }

}