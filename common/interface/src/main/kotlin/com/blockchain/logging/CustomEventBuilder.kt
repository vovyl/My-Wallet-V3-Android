package com.blockchain.logging

abstract class CustomEventBuilder(val eventName: String) {

    private val customEvents: MutableMap<String, String> = mutableMapOf()

    protected fun putCustomAttribute(key: String, value: String) {
        customEvents[key] = value
    }

    protected fun putCustomAttribute(key: String, value: Boolean) {
        customEvents[key] = value.toString()
    }

    fun build(action: (key: String, value: String) -> Unit) {
        customEvents.forEach { action(it.key, it.value) }
    }
}
