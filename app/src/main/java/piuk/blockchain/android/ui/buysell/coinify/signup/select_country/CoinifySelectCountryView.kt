package piuk.blockchain.android.ui.buysell.coinify.signup.select_country

import piuk.blockchain.androidcoreui.ui.base.View

interface CoinifySelectCountryView: View {

    fun onStartVerifyEmail(countryCode: String)

    fun onSetCountryPickerData(countryNameList: List<String>)

    fun onAutoSelectCountry(position: Int)
}