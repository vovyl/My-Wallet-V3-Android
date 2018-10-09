package com.blockchain.sunriver

import com.blockchain.testutils.lumens
import com.nhaarman.mockito_kotlin.mock
import info.blockchain.balance.AccountReference
import org.amshove.kluent.`it returns`
import org.amshove.kluent.`should equal`
import org.junit.Test

class XlmDataManagerTest {

    @Test
    fun `get balance`() {
        XlmDataManager(mock {
            on { getBalance("ANY") } `it returns` 123.lumens()
        })
            .getBalance(AccountReference.Xlm("", "ANY"))
            .test()
            .values() `should equal` listOf(123.lumens())
    }
}
