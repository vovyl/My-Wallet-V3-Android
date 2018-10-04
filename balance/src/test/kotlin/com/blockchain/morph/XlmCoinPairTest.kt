package com.blockchain.morph

import info.blockchain.balance.CryptoCurrency
import org.amshove.kluent.`should be`
import org.junit.Test

class XlmCoinPairTest {

    @Test
    fun `get pair XLM - XLM`() {
        CryptoCurrency.XLM to CryptoCurrency.XLM `should be` CoinPair.XLM_TO_XLM
    }

    @Test
    fun `get pair XLM - BTC`() {
        CryptoCurrency.XLM to CryptoCurrency.BTC `should be` CoinPair.XLM_TO_BTC
    }

    @Test
    fun `get pair BTC - XLM`() {
        CryptoCurrency.BTC to CryptoCurrency.XLM `should be` CoinPair.BTC_TO_XLM
    }

    @Test
    fun `get pair XLM - BCH`() {
        CryptoCurrency.XLM to CryptoCurrency.BCH `should be` CoinPair.XLM_TO_BCH
    }

    @Test
    fun `get pair BCH - XLM`() {
        CryptoCurrency.BCH to CryptoCurrency.XLM `should be` CoinPair.BCH_TO_XLM
    }

    @Test
    fun `get pair XLM - ETH`() {
        CryptoCurrency.XLM to CryptoCurrency.ETHER `should be` CoinPair.XLM_TO_ETH
    }

    @Test
    fun `get pair ETH - XLM`() {
        CryptoCurrency.ETHER to CryptoCurrency.XLM `should be` CoinPair.ETH_TO_XLM
    }
}
