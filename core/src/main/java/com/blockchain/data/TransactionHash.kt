package com.blockchain.data

import info.blockchain.balance.CryptoCurrency

data class TransactionHash(
    val cryptoCurrency: CryptoCurrency,
    val transactionHash: String
) {

    val explorerUrl: String get() = baseUrl() + transactionHash

    private fun baseUrl() =
        when (cryptoCurrency) {
            CryptoCurrency.BTC -> "https://www.blockchain.com/btc/tx/"
            CryptoCurrency.ETHER -> "https://etherscan.io/tx/"
            CryptoCurrency.BCH -> "https://blockchair.com/bitcoin-cash/transaction/"
            CryptoCurrency.XLM -> "https://stellarchain.io/tx/"
        }
}
