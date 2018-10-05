package piuk.blockchain.androidcoreui.utils.logging

internal fun CustomEventBuilder.buildToMap(): Map<String, String> {
    val map = mutableMapOf<String, String>()
    build { key, value -> map[key] = value }
    return map
}
