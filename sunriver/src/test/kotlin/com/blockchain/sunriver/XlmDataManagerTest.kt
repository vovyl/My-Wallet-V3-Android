package com.blockchain.sunriver

import com.blockchain.sunriver.datamanager.XlmAccount
import com.blockchain.sunriver.datamanager.XlmMetaData
import com.blockchain.sunriver.datamanager.XlmMetaDataInitializer
import com.blockchain.testutils.lumens
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import info.blockchain.balance.AccountReference
import info.blockchain.balance.CryptoValue
import io.reactivex.Single
import org.amshove.kluent.`it returns`
import org.amshove.kluent.`should equal`
import org.junit.Test

class XlmDataManagerTest {

    @Test
    fun `get balance`() {
        XlmDataManager(
            givenBalances("ANY" to 123.lumens()),
            mock()
        )
            .getBalance(AccountReference.Xlm("", "ANY"))
            .testSingle() `should equal` 123.lumens()
    }

    @Test
    fun `get balance without address`() {
        XlmDataManager(
            givenBalances("GABC1234" to 456.lumens()),
            givenMetaData(
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
        )
            .getBalance()
            .testSingle() `should equal` 456.lumens()
    }

    @Test
    fun `get default account 0`() {
        XlmDataManager(
            mock(),
            givenMetaData(
                XlmMetaData(
                    defaultAccountIndex = 0,
                    accounts = listOf(
                        XlmAccount(
                            publicKey = "ADDRESS1",
                            secret = "",
                            label = "Account #1",
                            archived = false
                        ),
                        XlmAccount(
                            publicKey = "ADDRESS2",
                            secret = "",
                            label = "Account #2",
                            archived = false
                        )
                    ),
                    transactionNotes = emptyMap()
                )
            )
        )
            .defaultAccount()
            .testSingle()
            .apply {
                label `should equal` "Account #1"
                accountId `should equal` "ADDRESS1"
            }
    }

    @Test
    fun `get default account 1`() {
        XlmDataManager(
            mock(),
            givenMetaData(
                XlmMetaData(
                    defaultAccountIndex = 1,
                    accounts = listOf(
                        XlmAccount(
                            publicKey = "ADDRESS1",
                            secret = "",
                            label = "Account #1",
                            archived = false
                        ),
                        XlmAccount(
                            publicKey = "ADDRESS2",
                            secret = "",
                            label = "Account #2",
                            archived = false
                        )
                    ),
                    transactionNotes = emptyMap()
                )
            )
        )
            .defaultAccount()
            .testSingle()
            .apply {
                label `should equal` "Account #2"
                accountId `should equal` "ADDRESS2"
            }
    }

    @Test
    fun `get default account 1 - balance`() {
        XlmDataManager(
            givenBalances(
                "ADDRESS1" to 10.lumens(),
                "ADDRESS2" to 20.lumens()
            ),
            givenMetaData(
                XlmMetaData(
                    defaultAccountIndex = 1,
                    accounts = listOf(
                        XlmAccount(
                            publicKey = "ADDRESS1",
                            secret = "",
                            label = "Account #1",
                            archived = false
                        ),
                        XlmAccount(
                            publicKey = "ADDRESS2",
                            secret = "",
                            label = "Account #2",
                            archived = false
                        )
                    ),
                    transactionNotes = emptyMap()
                )
            )
        )
            .getBalance()
            .testSingle() `should equal` 20.lumens()
    }

    @Test
    fun `get either balance by address`() {
        val xlmDataManager = XlmDataManager(
            givenBalances(
                "ADDRESS1" to 10.lumens(),
                "ADDRESS2" to 20.lumens()
            ),
            givenMetaData(
                XlmMetaData(
                    defaultAccountIndex = 1,
                    accounts = listOf(
                        XlmAccount(
                            publicKey = "ADDRESS1",
                            secret = "",
                            label = "Account #1",
                            archived = false
                        ),
                        XlmAccount(
                            publicKey = "ADDRESS2",
                            secret = "",
                            label = "Account #2",
                            archived = false
                        )
                    ),
                    transactionNotes = emptyMap()
                )
            )
        )
        xlmDataManager
            .getBalance(AccountReference.Xlm("", "ADDRESS1"))
            .testSingle() `should equal` 10.lumens()
        xlmDataManager
            .getBalance(AccountReference.Xlm("", "ADDRESS2"))
            .testSingle() `should equal` 20.lumens()
    }
}

private fun <T> Single<T>.testSingle() = test().values().single()

private fun givenBalances(
    vararg balances: Pair<String, CryptoValue>
): HorizonProxy {
    val horizonProxy: HorizonProxy = mock()
    balances.forEach { pair ->
        whenever(horizonProxy.getBalance(pair.first)) `it returns` pair.second
    }
    return horizonProxy
}

private fun givenMetaData(metaData: XlmMetaData): XlmMetaDataInitializer =
    mock {
        on { initWallet() } `it returns` Single.just(
            metaData
        )
    }
