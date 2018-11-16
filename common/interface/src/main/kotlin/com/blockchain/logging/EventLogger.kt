package com.blockchain.logging

interface EventLogger {

    fun logEvent(customEventBuilder: CustomEventBuilder)
}