package piuk.blockchain.android.ui.dashboard

import info.blockchain.balance.CryptoCurrency

object DashboardConfig {
    /**
     * The list of currencies and their order of display
     */
    val currencies = listOf(
        CryptoCurrency.BTC,
        CryptoCurrency.ETHER,
        CryptoCurrency.BCH,
        CryptoCurrency.XLM
    )
}