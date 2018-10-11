package com.blockchain.sunriver

import com.blockchain.sunriver.datamanager.XlmAccount
import com.blockchain.sunriver.datamanager.XlmMetaData
import com.blockchain.testutils.lumens
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import info.blockchain.balance.AccountReference
import io.reactivex.Single
import org.amshove.kluent.`it returns`
import org.amshove.kluent.`should equal`
import org.junit.Test

class XlmDataManagerTest {

    @Test
    fun `get balance`() {
        XlmDataManager(
            mock {
                on { getBalance("ANY") } `it returns` 123.lumens()
            },
            mock()
        )
            .getBalance(AccountReference.Xlm("", "ANY"))
            .test()
            .values()
            .single() `should equal` 123.lumens()
    }

    @Test
    fun `get balance without address`() {
        XlmDataManager(
            mock {
                on { getBalance("GABC1234") } `it returns` 456.lumens()
            },
            mock {
                on { initWallet(any()) } `it returns` Single.just(
                    XlmMetaData(
                        defaultAccountIndex = 0,
                        accounts = listOf(
                            XlmAccount(
                                publicKey = "GABC1234",
                                secret = "",
                                label = "",
                                archived = false
                            )
                        ),
                        transactionNotes = emptyMap()
                    )
                )
            }
        )
            .getBalance()
            .test()
            .values()
            .single() `should equal` 456.lumens()
    }
}
