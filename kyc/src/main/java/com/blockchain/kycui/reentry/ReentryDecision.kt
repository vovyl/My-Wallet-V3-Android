package com.blockchain.kycui.reentry

import com.blockchain.kyc.models.nabu.NabuUser
import com.blockchain.kyc.models.nabu.UserState

class ReentryDecision {

    internal fun findReentryPoint(user: NabuUser): ReentryPoint? {
        if (user.state == UserState.Active) {
            // All data is present and mobile verified, proceed to Onfido splash
            return ReentryPoint.Onfido
        }

        if (user.state == UserState.Created) {
            var allPoints = ReentryPoint.values().toSet()

            if (user.address?.countryCode != null) {
                allPoints -= ReentryPoint.CountrySelection
            }

            if (user.mobileVerified) {
                allPoints -= ReentryPoint.MobileEntry
            }

            if (user.mobile != null) {
                // If they entered a mobile already, they must have got past address
                allPoints -= ReentryPoint.Address
            }

            // Just go to earliest step that has not been completed
            return allPoints.toList().sorted().firstOrNull()
        }
        return null
    }
}
