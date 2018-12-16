package com.blockchain.kycui.reentry

import com.blockchain.kyc.models.nabu.Address
import com.blockchain.kyc.models.nabu.KycState
import com.blockchain.kyc.models.nabu.NabuUser
import com.blockchain.kyc.models.nabu.Tiers
import com.blockchain.kyc.models.nabu.UserState
import org.amshove.kluent.`should be`
import org.junit.Test

class ReentryDecisionTest {

    @Test
    fun `if email is unverified - go to email entry`() {
        whereNext(
            createdNabuUser(tier = 1).copy(
                email = "abc@def.com",
                emailVerified = false
            )
        ) `should be` ReentryPoint.EmailEntry
    }

    @Test
    fun `if country code is unset - go to country code entry`() {
        whereNext(
            createdNabuUser(tier = 1).copy(
                email = "abc@def.com",
                emailVerified = true
            )
        ) `should be` ReentryPoint.CountrySelection
    }

    @Test
    fun `if profile is not set - go to profile`() {
        whereNext(
            createdNabuUser(tier = 1).copy(
                email = "abc@def.com",
                emailVerified = true,
                address = Address(
                    line1 = "",
                    line2 = "",
                    city = "",
                    state = "",
                    postCode = "",
                    countryCode = "DE"
                )
            )
        ) `should be` ReentryPoint.Profile
    }

    @Test
    fun `if profile is set - go to address`() {
        whereNext(
            createdNabuUser(tier = 1).copy(
                email = "abc@def.com",
                emailVerified = true,
                address = Address(
                    line1 = "",
                    line2 = "",
                    city = "",
                    state = "",
                    postCode = "",
                    countryCode = "DE"
                ),
                dob = "dob",
                firstName = "A",
                lastName = "B"
            )
        ) `should be` ReentryPoint.Address
    }

    @Test
    fun `if user is tier 2, and mobile is not verified - go to mobile`() {
        whereNext(
            createdNabuUser(tier = 2).copy(
                mobile = "123456",
                mobileVerified = false
            )
        ) `should be` ReentryPoint.MobileEntry
    }

    @Test
    fun `if user is tier 2, and mobile is verified - go to onfido`() {
        whereNext(
            createdNabuUser(tier = 2).copy(
                mobile = "123456",
                mobileVerified = true
            )
        ) `should be` ReentryPoint.Onfido
    }

    private fun whereNext(user: NabuUser) =
        TiersReentryDecision().findReentryPoint(user)

    private fun createdNabuUser(tier: Int) =
        emptyNabuUser().copy(
            kycState = KycState.None,
            tiers = Tiers(
                current = tier - 1,
                next = tier,
                selected = tier
            )
        )

    private fun emptyNabuUser() =
        NabuUser(
            firstName = null,
            lastName = null,
            email = null,
            emailVerified = false,
            dob = null,
            mobile = null,
            mobileVerified = false,
            address = null,
            state = UserState.None,
            kycState = KycState.None,
            insertedAt = null
        )
}
