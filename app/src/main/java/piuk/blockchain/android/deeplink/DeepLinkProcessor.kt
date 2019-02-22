package piuk.blockchain.android.deeplink

import android.content.Intent
import com.blockchain.notifications.links.PendingLink
import io.reactivex.Maybe
import io.reactivex.Single
import piuk.blockchain.android.kyc.KycDeepLinkHelper
import piuk.blockchain.android.kyc.KycLinkState
import piuk.blockchain.android.sunriver.CampaignLinkState
import piuk.blockchain.android.sunriver.SunriverDeepLinkHelper

internal class DeepLinkProcessor(
    private val linkHandler: PendingLink,
    private val kycDeepLinkHelper: KycDeepLinkHelper,
    private val sunriverDeepLinkHelper: SunriverDeepLinkHelper
) {

    fun getLink(intent: Intent): Single<LinkState> =
        linkHandler.getPendingLinks(intent)
            .map { uri ->
                val sr = sunriverDeepLinkHelper.mapUri(uri)
                if (sr !is CampaignLinkState.NoUri) {
                    return@map LinkState.SunriverDeepLink(sr)
                }
                val kyc = kycDeepLinkHelper.mapUri(uri)
                if (kyc == KycLinkState.Resubmit) {
                    return@map LinkState.KycDeepLink(kyc)
                }
                LinkState.NoUri
            }
            .switchIfEmpty(Maybe.just(LinkState.NoUri))
            .toSingle()
            .onErrorResumeNext { Single.just(LinkState.NoUri) }
}

sealed class LinkState {
    data class SunriverDeepLink(val link: CampaignLinkState) : LinkState()
    data class KycDeepLink(val link: KycLinkState) : LinkState()
    object NoUri : LinkState()
}
