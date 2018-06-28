package piuk.blockchain.android.ui.buysell.payment.bank.addaccount

import android.support.annotation.StringRes
import piuk.blockchain.androidcoreui.ui.base.View
import piuk.blockchain.androidcoreui.ui.customviews.ToastCustom

interface AddBankAccountView : View {

    val iban: String

    val bic: String

    fun goToAddAddress(iban: String, bic: String)

    fun showToast(@StringRes message: Int, @ToastCustom.ToastType toastType: String)
}