package piuk.blockchain.androidbuysellui.ui.signup.select_country

import piuk.blockchain.androidcoreui.ui.base.View

interface SelectCountryView: View {

    fun onStartVerifyEmail(countryCode: String)

    fun onSetCountryPickerData(countryNameList: List<String>)

    fun onAutoSelectCountry(position: Int)
}