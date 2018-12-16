package com.blockchain.kycui.reentry

/**
 * Important, keep these in the correct order
 */
enum class ReentryPoint(val entryPoint: String) {
    EmailEntry("Email Entry"),
    CountrySelection("Country Selection"),
    Profile("Profile Entry"),
    Address("Address Entry"),
    MobileEntry("Mobile Entry"),
    Onfido("Onfido Splash")
}
