package com.blockchain.kycui.veriffsplash

import android.support.annotation.StringRes
import com.blockchain.kyc.models.nabu.SupportedDocuments
import com.blockchain.veriff.VeriffApplicantAndToken
import io.reactivex.Observable
import piuk.blockchain.androidcoreui.ui.base.View

interface VeriffSplashView : View {

    val countryCode: String

    val nextClick: Observable<Unit>

    fun continueToVeriff(
        applicant: VeriffApplicantAndToken
    )

    fun showProgressDialog(cancelable: Boolean)

    fun dismissProgressDialog()

    fun continueToCompletion()

    fun showErrorToast(@StringRes message: Int)

    fun supportedDocuments(documents: List<SupportedDocuments>)
}