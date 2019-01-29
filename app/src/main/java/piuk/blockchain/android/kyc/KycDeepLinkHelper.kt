package piuk.blockchain.android.kyc

import android.content.Intent
import com.blockchain.notifications.links.PendingLink
import io.reactivex.Maybe
import io.reactivex.Single

class KycDeepLinkHelper(
    private val linkHandler: PendingLink
) {

    fun getLink(intent: Intent): Single<KycLinkState> =
        linkHandler.getPendingLinks(intent)
            .map { uri ->
                val name = uri.getQueryParameter("deep_link_path")

                if (name != "verification") {
                    return@map KycLinkState.NoUri
                }
                KycLinkState.Data
            }
            .switchIfEmpty(Maybe.just(KycLinkState.NoUri))
            .toSingle()
            .onErrorResumeNext { Single.just(KycLinkState.NoUri) }
}

enum class KycLinkState {
    NoUri,
    Data
}
