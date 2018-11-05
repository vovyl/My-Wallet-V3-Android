package com.blockchain.sunriver.models

import com.blockchain.sunriver.HorizonKeyPair
import com.blockchain.testutils.lumens
import com.blockchain.testutils.stroops
import info.blockchain.balance.CryptoValue
import org.amshove.kluent.`should equal`
import org.junit.Test

class XlmTransactionTest {

    @Test
    fun `account delta on a positive value does not include fee`() {
        givenTransaction(value = 100.lumens(), fee = 99.stroops())
            .accountDelta `should equal` 100.lumens()
    }

    @Test
    fun `account delta on a negative value includes fee`() {
        givenTransaction(value = (-100).lumens(), fee = 99.stroops())
            .accountDelta `should equal` (-100.0000099).lumens()
    }

    private fun givenTransaction(
        value: CryptoValue,
        fee: CryptoValue
    ): XlmTransaction =
        XlmTransaction(
            timeStamp = "TIME",
            value = value,
            fee = fee,
            hash = "",
            to = HorizonKeyPair.Public("GA"),
            from = HorizonKeyPair.Public("GB")
        )
}
