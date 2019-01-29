package com.blockchain.kycui.sunriver

import com.blockchain.android.testutils.rxInit
import com.blockchain.kyc.models.nabu.CampaignData
import com.blockchain.kyc.models.nabu.KycState
import com.blockchain.kyc.models.nabu.RegisterCampaignRequest
import com.blockchain.kyc.models.nabu.UserState
import com.blockchain.nabu.metadata.NabuCredentialsMetadata
import com.blockchain.nabu.metadata.NabuCredentialsMetadata.Companion.USER_CREDENTIALS_METADATA_NODE
import com.blockchain.nabu.models.NabuOfflineTokenResponse
import com.blockchain.nabu.models.mapFromMetadata
import com.blockchain.serialization.toMoshiJson
import com.google.common.base.Optional
import com.nhaarman.mockito_kotlin.mock
import info.blockchain.balance.AccountReference
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import org.amshove.kluent.`it returns`
import org.amshove.kluent.`should equal`
import org.amshove.kluent.any
import org.amshove.kluent.mock
import org.junit.Rule
import org.junit.Test

class SunriverCampaignHelperTest {

    @get:Rule
    val initSchedulers = rxInit {
        ioTrampoline()
    }

    @Test
    fun `get card type none as campaign disabled`() {
        SunriverCampaignHelper(
            mock {
                on { enabled } `it returns` Single.just(false)
            },
            mock(),
            mock(),
            mock()
        ).getCampaignCardType()
            .test()
            .values()
            .first()
            .apply {
                this `should equal` SunriverCardType.None
            }
    }

    @Test
    fun `get card type complete`() {
        val offlineToken = NabuCredentialsMetadata("userId", "token")
        SunriverCampaignHelper(
            mock {
                on { enabled } `it returns` Single.just(true)
            },
            mock {
                on { getCampaignList(offlineToken.mapFromMetadata()) } `it returns` Single.just(listOf("SUNRIVER"))
            },
            mock {
                on { fetchMetadata(USER_CREDENTIALS_METADATA_NODE) } `it returns` Observable.just(
                    Optional.of(offlineToken.toMoshiJson())
                )
            },
            mock {
                on { getUserState() } `it returns` Single.just<UserState>(UserState.Active)
                on { getKycStatus() } `it returns` Single.just<KycState>(KycState.Verified)
            }
        ).getCampaignCardType()
            .test()
            .values()
            .first()
            .apply {
                this `should equal` SunriverCardType.Complete
            }
    }

    @Test
    fun `get card type join waitlist`() {
        val offlineToken = NabuCredentialsMetadata("userId", "token")
        SunriverCampaignHelper(
            mock {
                on { enabled } `it returns` Single.just(true)
            },
            mock {
                on { getCampaignList(offlineToken.mapFromMetadata()) } `it returns` Single.just(emptyList())
            },
            mock {
                on { fetchMetadata(USER_CREDENTIALS_METADATA_NODE) } `it returns` Observable.just(
                    Optional.of(offlineToken.toMoshiJson())
                )
            },
            mock {
                on { getUserState() } `it returns` Single.just<UserState>(UserState.Active)
                on { getKycStatus() } `it returns` Single.just<KycState>(KycState.None)
            }
        ).getCampaignCardType()
            .test()
            .values()
            .first()
            .apply {
                this `should equal` SunriverCardType.JoinWaitList
            }
    }

    @Test
    fun `get card type finish sign up`() {
        val offlineToken = NabuCredentialsMetadata("userId", "token")
        SunriverCampaignHelper(
            mock {
                on { enabled } `it returns` Single.just(true)
            },
            mock {
                on { getCampaignList(offlineToken.mapFromMetadata()) } `it returns` Single.just(listOf("SUNRIVER"))
            },
            mock {
                on { fetchMetadata(USER_CREDENTIALS_METADATA_NODE) } `it returns` Observable.just(
                    Optional.of(offlineToken.toMoshiJson())
                )
            },
            mock {
                on { getUserState() } `it returns` Single.just<UserState>(UserState.Created)
                on { getKycStatus() } `it returns` Single.just<KycState>(KycState.None)
            }
        ).getCampaignCardType()
            .test()
            .values()
            .first()
            .apply {
                this `should equal` SunriverCardType.FinishSignUp
            }
    }

    @Test
    fun `register as user already has an account`() {
        val offlineToken = NabuCredentialsMetadata("userId", "token")
        val accountRef = AccountReference.Xlm("", "")
        val campaignData = CampaignData("name", false)
        SunriverCampaignHelper(
            mock(),
            mock {
                on {
                    registerCampaign(
                        offlineToken.mapFromMetadata(),
                        RegisterCampaignRequest.registerSunriver(
                            accountRef.accountId,
                            campaignData.newUser
                        ),
                        campaignData.campaignName
                    )
                } `it returns` Completable.complete()
            },
            mock {
                on { fetchMetadata(USER_CREDENTIALS_METADATA_NODE) } `it returns` Observable.just(
                    Optional.of(offlineToken.toMoshiJson())
                )
            },
            mock()
        ).registerCampaignAndSignUpIfNeeded(accountRef, campaignData)
            .test()
            .assertNoErrors()
            .assertComplete()
    }

    @Test
    fun `register as user has no account`() {
        val emptyToken = NabuCredentialsMetadata("", "")
        val validToken = NabuOfflineTokenResponse("userId", "token")
        val accountRef = AccountReference.Xlm("", "")
        val campaignData = CampaignData("name", false)
        SunriverCampaignHelper(
            mock(),
            mock {
                on {
                    registerCampaign(
                        validToken,
                        RegisterCampaignRequest.registerSunriver(
                            accountRef.accountId,
                            campaignData.newUser
                        ),
                        campaignData.campaignName
                    )
                } `it returns` Completable.complete()
                on { requestJwt() } `it returns` Single.just("jwt")
                on { getAuthToken("jwt") } `it returns` Single.just(validToken)
            },
            mock {
                on { fetchMetadata(USER_CREDENTIALS_METADATA_NODE) } `it returns` Observable.just(
                    Optional.of(emptyToken.toMoshiJson())
                )
                on { saveToMetadata(any()) } `it returns` Completable.complete()
            },
            mock()
        ).registerCampaignAndSignUpIfNeeded(accountRef, campaignData)
            .test()
            .assertNoErrors()
            .assertComplete()
    }
}