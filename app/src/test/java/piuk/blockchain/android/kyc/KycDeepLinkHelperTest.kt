package piuk.blockchain.android.kyc

import android.net.Uri
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

@Config(sdk = [23], constants = BuildConfig::class, application = BlockchainTestApplication::class)
@RunWith(RobolectricTestRunner::class)
class KycDeepLinkHelperTest {

    @Test
    fun `no uri`() {
        KycDeepLinkHelper(
            mock {
                on { getPendingLinks(any()) } `it returns` Maybe.empty()
            }
        ).getLink(mock())
            .test()
            .assertNoErrors()
            .assertValue(KycLinkState.NoUri)
    }

    @Test
    fun `not a resubmission uri`() {
        KycDeepLinkHelper(givenPendingUri("https://login.blockchain.com/#/open/referral?campaign=sunriver"))
            .getLink(mock())
            .test()
            .assertNoErrors()
            .assertValue(KycLinkState.NoUri)
    }

    @Test
    fun `extract that it is a resubmission deeplink`() {
        KycDeepLinkHelper(givenPendingUri("https://login.blockchain.com/login?deep_link_path=verification"))
            .getLink(mock())
            .test()
            .assertNoErrors()
            .assertValue(KycLinkState.Resubmit)
    }
}

private fun givenPendingUri(uri: String): PendingLink =
    mock {
        on { getPendingLinks(any()) } `it returns` Maybe.just(Uri.parse(uri))
    }