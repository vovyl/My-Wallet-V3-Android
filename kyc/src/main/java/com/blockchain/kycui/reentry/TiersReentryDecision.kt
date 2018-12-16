package com.blockchain.kycui.reentry

import com.blockchain.kyc.models.nabu.NabuUser

class TiersReentryDecision : ReentryDecision {

    override fun findReentryPoint(user: NabuUser): ReentryPoint {
        if (user.tiers?.current == 1) {

            if (!user.mobileVerified) {
                return ReentryPoint.MobileEntry
            }

            return ReentryPoint.Onfido
        }

        if (user.emailVerified != true) {
            return ReentryPoint.EmailEntry
        }

        if (user.address?.countryCode == null) {
            return ReentryPoint.CountrySelection
        }

        if (user.firstName.isNullOrBlank() || user.lastName.isNullOrBlank() || user.dob.isNullOrBlank()) {
            return ReentryPoint.Profile
        }

        return ReentryPoint.Address
    }
}
