package piuk.blockchain.androidcoreui.utils.logging

class BalanceLoadedEvent(
    hasBtcBalance: Boolean,
    hasBchBalance: Boolean,
    hasEthBalance: Boolean,
    hasXlmBalance: Boolean
) : CustomEventBuilder("Balances loaded") {

    init {
        putCustomAttribute("Has BTC balance", hasBtcBalance)
        putCustomAttribute("Has BCH balance", hasBchBalance)
        putCustomAttribute("Has ETH balance", hasEthBalance)
        putCustomAttribute("Has XLM balance", hasXlmBalance)

        val hasAnyBalance = hasBtcBalance || hasBchBalance || hasEthBalance || hasXlmBalance

        putCustomAttribute("Has any balance", hasAnyBalance)
    }
}
