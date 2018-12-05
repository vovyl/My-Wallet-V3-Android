package com.blockchain.kycui.invalidcountry

import com.blockchain.kycui.countryselection.util.CountryDisplayModel
import piuk.blockchain.androidcoreui.ui.base.View

interface KycInvalidCountryView : View {

    val displayModel: CountryDisplayModel

    fun showProgressDialog()

    fun dismissProgressDialog()

    fun finishPage()
}