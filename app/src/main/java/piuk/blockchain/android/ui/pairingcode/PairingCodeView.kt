package piuk.blockchain.android.ui.pairingcode

import android.graphics.Bitmap
import android.support.annotation.StringRes

import piuk.blockchain.androidcoreui.ui.base.View
import piuk.blockchain.androidcoreui.ui.customviews.ToastCustom

interface PairingCodeView : View {

    fun onQrLoaded(bitmap: Bitmap)

    fun showToast(@StringRes message: Int, @ToastCustom.ToastType toastType: String)

    fun showProgressSpinner()

    fun hideProgressSpinner()
}
