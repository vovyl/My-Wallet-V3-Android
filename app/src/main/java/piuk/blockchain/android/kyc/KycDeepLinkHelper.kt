package piuk.blockchain.android.kyc

import android.content.Intent
import android.net.Uri
import com.blockchain.notifications.links.PendingLink
import io.reactivex.Maybe
import io.reactivex.Single

class KycDeepLinkHelper(
    private val linkHandler: PendingLink
) {

    fun getLink(intent: Intent): Single<KycLinkState> =
        linkHandler.getPendingLinks(intent)
            .map(this::mapUri)
            .switchIfEmpty(Maybe.just(KycLinkState.NoUri))
            .toSingle()
            .onErrorResumeNext { Single.just(KycLinkState.NoUri) }

    fun mapUri(uri: Uri): KycLinkState {
        val name = uri.getQueryParameter("deep_link_path")

        return if (name != "verification") {
            KycLinkState.NoUri
        } else {
            KycLinkState.Resubmit
        }
    }
}

enum class KycLinkState {
    NoUri,
    Resubmit
}
