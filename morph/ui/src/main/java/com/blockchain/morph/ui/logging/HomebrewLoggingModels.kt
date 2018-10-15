package com.blockchain.morph.ui.logging

import com.crashlytics.android.answers.CustomEvent

internal class FixTypeEvent(fixType: FixType) : CustomEvent("Fix type switched") {

    init {
        putCustomAttribute("Input Type", fixType.name)
    }
}

internal enum class FixType(val type: String) {
    BaseFiat("Base fiat"),
    BaseCrypto("Base crypto"),
    CounterFiat("Counter fiat"),
    CounterCrypto("Counter crypto")
}

internal class AccountSwapEvent : CustomEvent("Account to/from swapped") {

    init {
        putCustomAttribute("Account swapped", "true")
    }
}

internal class MarketRatesViewedEvent : CustomEvent("Market rates viewed") {

    init {
        putCustomAttribute("Rates viewed", "true")
    }
}

internal class WebsocketConnectionFailureEvent : CustomEvent("Websocket connection failure") {

    init {
        putCustomAttribute("Websocket connection failed", "true")
    }
}

internal class AmountErrorEvent(errorType: AmountErrorType) : CustomEvent("Min/Max error") {

    init {
        putCustomAttribute("Min/Max error type", errorType.error)
    }
}

internal enum class AmountErrorType(val error: String) {
    OverBalance("Over user's balance"),
    OverMax("Over max"),
    UnderMin("Under min")
}