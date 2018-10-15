package com.blockchain.kycui.logging

import com.crashlytics.android.answers.CustomEvent

internal class KycResumedEvent(entryPoint: ReentryPoint) : CustomEvent("User Resumed KYC flow") {

    init {
        putCustomAttribute("User resumed KYC", entryPoint.entryPoint)
    }
}

internal enum class ReentryPoint(val entryPoint: String) {
    Onfido("Onfido Splash"),
    MobileEntry("Mobile Entry"),
    Address("Address Entry"),
    CountrySelection("Country Selection")
}