package com.blockchain.kycui.logging

import com.blockchain.kycui.reentry.ReentryPoint
import com.crashlytics.android.answers.CustomEvent

internal class KycResumedEvent(entryPoint: ReentryPoint) : CustomEvent("User Resumed KYC flow") {

    init {
        putCustomAttribute("User resumed KYC", entryPoint.entryPoint)
    }
}
