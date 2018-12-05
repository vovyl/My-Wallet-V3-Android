package com.blockchain.morph

import info.blockchain.balance.CryptoCurrency

enum class CoinPair(
    val pairCode: String,
    val from: CryptoCurrency,
    val to: CryptoCurrency,
    val pairCodeUpper: String = pairCode.toUpperCase().replace("_", "-")
) {

    BTC_TO_BTC("btc_btc", CryptoCurrency.BTC, CryptoCurrency.BTC),
    BTC_TO_ETH("btc_eth", CryptoCurrency.BTC, CryptoCurrency.ETHER),
    BTC_TO_BCH("btc_bch", CryptoCurrency.BTC, CryptoCurrency.BCH),
    BTC_TO_XLM("btc_xlm", CryptoCurrency.BTC, CryptoCurrency.XLM),

    ETH_TO_ETH("eth_eth", CryptoCurrency.ETHER, CryptoCurrency.ETHER),
    ETH_TO_BTC("eth_btc", CryptoCurrency.ETHER, CryptoCurrency.BTC),
    ETH_TO_BCH("eth_bch", CryptoCurrency.ETHER, CryptoCurrency.BCH),
    ETH_TO_XLM("eth_xlm", CryptoCurrency.ETHER, CryptoCurrency.XLM),

    BCH_TO_BCH("bch_bch", CryptoCurrency.BCH, CryptoCurrency.BCH),
    BCH_TO_BTC("bch_btc", CryptoCurrency.BCH, CryptoCurrency.BTC),
    BCH_TO_ETH("bch_eth", CryptoCurrency.BCH, CryptoCurrency.ETHER),
    BCH_TO_XLM("bch_xlm", CryptoCurrency.BCH, CryptoCurrency.XLM),

    XLM_TO_XLM("xlm_xlm", CryptoCurrency.XLM, CryptoCurrency.XLM),
    XLM_TO_BTC("xlm_btc", CryptoCurrency.XLM, CryptoCurrency.BTC),
    XLM_TO_ETH("xlm_eth", CryptoCurrency.XLM, CryptoCurrency.ETHER),
    XLM_TO_BCH("xlm_bch", CryptoCurrency.XLM, CryptoCurrency.BCH);

    val sameInputOutput = from == to

    fun inverse() = to to from

    companion object {

        fun fromPairCode(pairCode: String): CoinPair {
            return fromPairCodeOrNull(pairCode) ?: throw IllegalStateException("Attempt to get invalid pair $pairCode")
        }

        fun fromPairCodeOrNull(pairCode: String?): CoinPair? {
            pairCode?.split('_')?.let {
                if (it.size == 2) {
                    val from = CryptoCurrency.fromSymbol(it.first())
                    val to = CryptoCurrency.fromSymbol(it.last())
                    if (from != null && to != null) {
                        return from to to
                    }
                }
            }
            return null
        }
    }
}

infix fun CryptoCurrency.to(other: CryptoCurrency) =
    when (this) {
        CryptoCurrency.BTC -> when (other) {
            CryptoCurrency.BTC -> CoinPair.BTC_TO_BTC
            CryptoCurrency.ETHER -> CoinPair.BTC_TO_ETH
            CryptoCurrency.BCH -> CoinPair.BTC_TO_BCH
            CryptoCurrency.XLM -> CoinPair.BTC_TO_XLM
        }
        CryptoCurrency.ETHER -> when (other) {
            CryptoCurrency.ETHER -> CoinPair.ETH_TO_ETH
            CryptoCurrency.BTC -> CoinPair.ETH_TO_BTC
            CryptoCurrency.BCH -> CoinPair.ETH_TO_BCH
            CryptoCurrency.XLM -> CoinPair.ETH_TO_XLM
        }
        CryptoCurrency.BCH -> when (other) {
            CryptoCurrency.BCH -> CoinPair.BCH_TO_BCH
            CryptoCurrency.BTC -> CoinPair.BCH_TO_BTC
            CryptoCurrency.ETHER -> CoinPair.BCH_TO_ETH
            CryptoCurrency.XLM -> CoinPair.BCH_TO_XLM
        }
        CryptoCurrency.XLM -> when (other) {
            CryptoCurrency.XLM -> CoinPair.XLM_TO_XLM
            CryptoCurrency.BTC -> CoinPair.XLM_TO_BTC
            CryptoCurrency.ETHER -> CoinPair.XLM_TO_ETH
            CryptoCurrency.BCH -> CoinPair.XLM_TO_BCH
        }
    }
