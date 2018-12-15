package com.blockchain.kycui.reentry

import com.blockchain.kyc.models.nabu.Address
import com.blockchain.kyc.models.nabu.KycState
import com.blockchain.kyc.models.nabu.NabuUser
import com.blockchain.kyc.models.nabu.UserState
import org.amshove.kluent.`should be`
import org.junit.Test

class ReentryDecisionTest {

    @Test
    fun `a new user should not be reentered in to the flow`() {
        whereNext(emptyNabuUser()) `should be` null
    }

    @Test
    fun `a created user with no country code`() {
        whereNext(
            emptyNabuUser().copy(state = UserState.Created)
        ) `should be` ReentryPoint.CountrySelection
    }

    @Test
    fun `an active user`() {
        whereNext(
            emptyNabuUser().copy(state = UserState.Active)
        ) `should be` ReentryPoint.Onfido
    }

    @Test
    fun `a created user with a country code and no mobile`() {
        whereNext(
            emptyNabuUser().copy(
                state = UserState.Created,
                address = Address(
                    line1 = "",
                    line2 = "",
                    city = "",
                    state = "",
                    postCode = "",
                    countryCode = "DE"
                ),
                mobile = null
            )
        ) `should be` ReentryPoint.Address
    }

    @Test
    fun `a created user with a country code and an unverified mobile`() {
        whereNext(
            emptyNabuUser().copy(
                state = UserState.Created,
                address = Address(
                    line1 = "",
                    line2 = "",
                    city = "",
                    state = "",
                    postCode = "",
                    countryCode = "DE"
                ),
                mobile = "1234"
            )
        ) `should be` ReentryPoint.MobileEntry
    }

    @Test
    fun `a created user with a country code and a verified mobile`() {
        whereNext(
            emptyNabuUser().copy(
                state = UserState.Created,
                address = Address(
                    line1 = "",
                    line2 = "",
                    city = "",
                    state = "",
                    postCode = "",
                    countryCode = "DE"
                ),
                mobile = "1234",
                mobileVerified = true
            )
        ) `should be` ReentryPoint.Onfido
    }

    private fun whereNext(user: NabuUser) =
        ReentryDecision().findReentryPoint(user)

    private fun emptyNabuUser() =
        NabuUser(
            firstName = null,
            lastName = null,
            email = null,
            mobile = null,
            dob = null,
            mobileVerified = false,
            address = null,
            state = UserState.None,
            kycState = KycState.None,
            insertedAt = null
        )
}
