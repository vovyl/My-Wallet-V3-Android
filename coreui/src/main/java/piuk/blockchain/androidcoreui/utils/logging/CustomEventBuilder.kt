package piuk.blockchain.androidcoreui.utils.logging

abstract class CustomEventBuilder(internal val eventName: String) {

    private val customEvents: MutableMap<String, String> = mutableMapOf()

    protected fun putCustomAttribute(key: String, value: String) {
        customEvents[key] = value
    }

    protected fun putCustomAttribute(key: String, value: Boolean) {
        customEvents[key] = value.toString()
    }

    internal fun build(action: (key: String, value: String) -> Unit) {
        customEvents.forEach { action(it.key, it.value) }
    }
}
