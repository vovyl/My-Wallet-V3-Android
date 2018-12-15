package com.blockchain.kycui.tiersplash

import android.support.annotation.StringRes
import com.blockchain.kyc.models.nabu.TiersJson

interface KycTierSplashView : piuk.blockchain.androidcoreui.ui.base.View {

    fun startEmailVerification()

    fun showErrorToast(@StringRes message: Int)

    fun renderTiersList(tiers: TiersJson)
}
