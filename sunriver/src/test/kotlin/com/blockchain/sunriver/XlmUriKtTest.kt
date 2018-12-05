package com.blockchain.sunriver

import info.blockchain.balance.AccountReference
import org.amshove.kluent.`should equal to`
import org.junit.Test

class XlmUriKtTest {

    @Test
    fun `returns valid uri`() {
        AccountReference.Xlm(
            "label",
            "accountId"
        ).toUri() `should equal to` "web+stellar:pay?destination=accountId"
    }
}