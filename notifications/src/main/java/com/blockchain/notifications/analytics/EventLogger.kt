package com.blockchain.notifications.analytics

interface EventLogger {

    fun logEvent(loggable: Loggable)
}

interface Loggable {

    val eventName: String
}