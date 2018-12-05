package com.blockchain.notifications.analytics

import com.google.firebase.analytics.FirebaseAnalytics

class Analytics internal constructor(
    private val firebaseAnalytics: FirebaseAnalytics
) : EventLogger {

    override fun logEvent(loggable: Loggable) {
        firebaseAnalytics.logEvent(loggable.eventName, null)
    }
}