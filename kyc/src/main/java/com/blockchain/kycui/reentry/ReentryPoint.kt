package com.blockchain.kycui.reentry

/**
 * Important, keep these in the correct order
 */
internal enum class ReentryPoint(val entryPoint: String) {
    CountrySelection("Country Selection"),
    Address("Address Entry"),
    MobileEntry("Mobile Entry"),
    Onfido("Onfido Splash")
}
