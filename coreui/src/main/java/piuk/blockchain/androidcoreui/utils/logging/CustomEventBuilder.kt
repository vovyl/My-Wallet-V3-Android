package piuk.blockchain.androidcoreui.utils.logging

abstract class CustomEventBuilder(internal val eventName: String) {

    private val customEvents = mutableMapOf<String, String>()

    protected fun putCustomAttribute(key: String, value: String) {
        customEvents[key] = value
    }

    internal fun build(action: (key: String, value: String) -> Unit) {
        customEvents.forEach(action)
    }
}
