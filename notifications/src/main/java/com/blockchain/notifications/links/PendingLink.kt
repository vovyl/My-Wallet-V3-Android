package com.blockchain.notifications.links

import android.content.Intent
import android.net.Uri
import io.reactivex.Maybe

interface PendingLink {

    fun getPendingLinks(intent: Intent): Maybe<Uri>
}