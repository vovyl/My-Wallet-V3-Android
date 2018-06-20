package piuk.blockchain.android.ui.buysell.payment.bank.addaddress

import android.support.annotation.StringRes
import piuk.blockchain.androidcoreui.ui.base.View
import piuk.blockchain.androidcoreui.ui.customviews.ToastCustom

interface AddAddressView : View {

    val iban: String

    val bic: String

    val accountHolderName: String

    val streetAndNumber: String

    val city: String

    val postCode: String

    val countryCodePosition: Int

    fun showToast(@StringRes message: Int, @ToastCustom.ToastType toastType: String)

    fun showProgressDialog()

    fun dismissProgressDialog()

    fun showErrorDialog(errorDescription: String)

    fun setCountryPickerData(countryList: List<String>)

    fun onAutoSelectCountry(position: Int, country: String)

    fun goToConfirmation()
}