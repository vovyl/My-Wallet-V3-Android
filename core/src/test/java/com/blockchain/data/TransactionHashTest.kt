package com.blockchain.data

import info.blockchain.balance.CryptoCurrency
import org.amshove.kluent.`should equal`
import org.junit.Test

class TransactionHashTest {

    @Test
    fun `btc transaction url`() {
        TransactionHash(
            CryptoCurrency.BTC,
            transactionHash = "5df87595c4ea6c2535c77e04ccba737e051fae5a967a1be28837e158b52a6f82"
        ).explorerUrl `should equal`
            "https://www.blockchain.com/btc/tx/5df87595c4ea6c2535c77e04ccba737e051fae5a967a1be28837e158b52a6f82"
    }

    @Test
    fun `bch transaction url`() {
        TransactionHash(
            CryptoCurrency.BCH,
            transactionHash = "c033fce668b3ceb5da71ea089d5416d32807d78eeabd9279fa39ba79cef2abb0"
        ).explorerUrl `should equal`
            "https://www.blockchain.com/bch/tx/c033fce668b3ceb5da71ea089d5416d32807d78eeabd9279fa39ba79cef2abb0"
    }

    @Test
    fun `eth transaction url`() {
        TransactionHash(
            CryptoCurrency.ETHER,
            transactionHash = "0x15c732c7fd81b03b3b600aeb6b820b66d74cc9aa8ce4a86c1401708cbce404d1"
        ).explorerUrl `should equal`
            "https://www.blockchain.com/eth/tx/0x15c732c7fd81b03b3b600aeb6b820b66d74cc9aa8ce4a86c1401708cbce404d1"
    }

    @Test
    fun `xlm transaction url`() {
        TransactionHash(
            CryptoCurrency.XLM,
            transactionHash = "8fe5374dfd828f3afbf0a6bb014c6fbf7f6207fe43015f270550d82db473384c"
        ).explorerUrl `should equal`
            "https://stellarchain.io/tx/8fe5374dfd828f3afbf0a6bb014c6fbf7f6207fe43015f270550d82db473384c"
    }
}