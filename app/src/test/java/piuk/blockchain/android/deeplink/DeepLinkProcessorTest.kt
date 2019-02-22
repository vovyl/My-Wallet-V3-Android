package piuk.blockchain.android.deeplink

import android.net.Uri
import com.blockchain.kyc.models.nabu.CampaignData
import com.blockchain.notifications.links.PendingLink
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
import piuk.blockchain.android.kyc.KycDeepLinkHelper
import piuk.blockchain.android.kyc.KycLinkState
import piuk.blockchain.android.sunriver.CampaignLinkState
import piuk.blockchain.android.sunriver.SunriverDeepLinkHelper

@Config(sdk = [23], constants = BuildConfig::class, application = BlockchainTestApplication::class)
@RunWith(RobolectricTestRunner::class)
class DeepLinkProcessorTest {

    @Test
    fun `unknown uri`() {
        givenUriExpect(
            "https://login.blockchain.com/", LinkState.NoUri
        )
    }

    @Test
    fun `sunriver uri`() {
        givenUriExpect(
            "https://login.blockchain.com/#/open/referral?campaign=sunriver", LinkState.SunriverDeepLink(
                CampaignLinkState.Data(CampaignData("sunriver", newUser = false))
            )
        )
    }

    @Test
    fun `kyc resubmit uri`() {
        givenUriExpect(
            "https://login.blockchain.com/login?deep_link_path=verification", LinkState.KycDeepLink(
                KycLinkState.Resubmit
            )
        )
    }
}

private fun givenUriExpect(uri: String, expected: LinkState) {
    DeepLinkProcessor(
        givenPendingUri(uri),
        KycDeepLinkHelper(mock()),
        SunriverDeepLinkHelper(mock())
    ).getLink(mock())
        .test()
        .assertNoErrors()
        .assertValue(expected)
}

private fun givenPendingUri(uri: String): PendingLink =
    mock {
        on { getPendingLinks(any()) } `it returns` Maybe.just(Uri.parse(uri))
    }
