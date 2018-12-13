package com.blockchain.kycui.tiersplash

import android.support.annotation.StringRes

interface KycTierSplashView : piuk.blockchain.androidcoreui.ui.base.View {

    fun startEmailVerification()

    fun showErrorToast(@StringRes message: Int)
}
