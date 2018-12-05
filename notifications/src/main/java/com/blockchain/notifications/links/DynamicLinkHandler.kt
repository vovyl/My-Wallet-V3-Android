package com.blockchain.notifications.links

import android.content.Intent
import android.net.Uri
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import io.reactivex.Maybe

internal class DynamicLinkHandler internal constructor(
    private val dynamicLinks: FirebaseDynamicLinks
) : PendingLink {

    override fun getPendingLinks(intent: Intent): Maybe<Uri> = Maybe.create { emitter ->
        dynamicLinks.getDynamicLink(intent)
            .addOnSuccessListener { linkData ->
                if (!emitter.isDisposed) linkData?.link?.let { emitter.onSuccess(it) } ?: emitter.onComplete()
            }
            .addOnFailureListener { if (!emitter.isDisposed) emitter.onError(it) }
    }
}