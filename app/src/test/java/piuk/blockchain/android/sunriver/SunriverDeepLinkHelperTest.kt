package piuk.blockchain.android.sunriver

import android.net.Uri
import com.blockchain.kyc.models.nabu.CampaignData
import com.nhaarman.mockito_kotlin.mock
import io.reactivex.Maybe
import org.amshove.kluent.`it returns`
import org.amshove.kluent.any
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import piuk.blockchain.android.BlockchainTestApplication
import piuk.blockchain.android.BuildConfig

@Config(sdk = [23], constants = BuildConfig::class, application = BlockchainTestApplication::class)
@RunWith(RobolectricTestRunner::class)
class SunriverDeepLinkHelperTest {

    @Test
    fun `returns no URI as no link found`() {
        SunriverDeepLinkHelper(
            mock {
                on { getPendingLinks(any()) } `it returns` Maybe.empty()
            }
        ).getCampaignCode(mock())
            .test()
            .assertNoErrors()
            .assertValue(CampaignLinkState.NoUri)
    }

    @Test
    fun `returns no URI as link doesn't contain URI data`() {
        SunriverDeepLinkHelper(
            mock {
                on { getPendingLinks(any()) } `it returns` Maybe.just(
                    Uri.parse("https://login.blockchain.com/")
                )
            }
        ).getCampaignCode(mock())
            .test()
            .assertNoErrors()
            .assertValue(CampaignLinkState.NoUri)
    }

    @Test
    fun `returns params as data class, not a new user`() {
        SunriverDeepLinkHelper(
            mock {
                on { getPendingLinks(any()) } `it returns` Maybe.just(
                    Uri.parse(
                        "https://login.blockchain.com/#/open/referral?campaign=sunriver"
                    )
                )
            }
        ).getCampaignCode(mock())
            .test()
            .assertNoErrors()
            .assertValue(
                CampaignLinkState.Data(
                    CampaignData("sunriver", false)
                )
            )
    }

    @Test
    fun `returns params as data class, is a new user`() {
        SunriverDeepLinkHelper(
            mock {
                on { getPendingLinks(any()) } `it returns` Maybe.just(
                    Uri.parse(
                        "https://login.blockchain.com/#/open/referral?" +
                            "campaign=sunriver&newUser=true"
                    )
                )
            }
        ).getCampaignCode(mock())
            .test()
            .assertNoErrors()
            .assertValue(
                CampaignLinkState.Data(
                    CampaignData("sunriver", true)
                )
            )
    }

    @Test
    fun `not a referral link`() {
        SunriverDeepLinkHelper(
            mock {
                on { getPendingLinks(any()) } `it returns` Maybe.just(
                    Uri.parse("https://login.blockchain.com/#/open/resubmission?campaign=sunriver&newUser=true")
                )
            }
        ).getCampaignCode(mock())
            .test()
            .assertNoErrors()
            .assertValue(CampaignLinkState.NoUri)
    }
}