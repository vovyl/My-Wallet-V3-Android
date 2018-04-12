package piuk.blockchain.androidcore.data.api

import info.blockchain.wallet.api.Environment
import org.bitcoinj.core.NetworkParameters

interface EnvironmentConfig {
    val environment: Environment
    val explorerUrl: String
    val apiUrl: String
    val btcWebsocketUrl: String
    val ethWebsocketUrl: String
    val bchWebsocketUrl: String
    val bitcoinNetworkParameters: NetworkParameters
    val bitcoinCashNetworkParameters: NetworkParameters

    fun shouldShowDebugMenu(): Boolean
}