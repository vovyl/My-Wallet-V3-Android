package com.blockchain.kycui.onfidosplash

import android.support.annotation.StringRes
import com.blockchain.kyc.models.nabu.SupportedDocuments
import io.reactivex.Observable
import piuk.blockchain.androidcoreui.ui.base.View

interface OnfidoSplashView : View {

    val uiState: Observable<String>

    fun continueToOnfido(
        apiKey: String,
        applicantId: String,
        supportedDocuments: List<SupportedDocuments>
    )

    fun showProgressDialog(cancelable: Boolean)

    fun dismissProgressDialog()

    fun continueToCompletion()

    fun showErrorToast(@StringRes message: Int)
}