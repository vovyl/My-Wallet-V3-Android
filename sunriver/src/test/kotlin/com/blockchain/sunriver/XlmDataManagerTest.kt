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
import org.amshove.kluent.mock
import org.junit.Test
import org.stellar.sdk.KeyPair
import org.stellar.sdk.responses.operations.CreateAccountOperationResponse
import org.stellar.sdk.responses.operations.OperationResponse
import org.stellar.sdk.responses.operations.PaymentOperationResponse
import org.stellar.sdk.responses.operations.SetOptionsOperationResponse

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

    @Test
    fun `get transaction list from default account`() {
        XlmDataManager(
            givenTransactions("GABC1234" to getResponseList()),
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
            .getTransactionList()
            .testSingle() `should equal` getXlmList()
    }

    @Test
    fun `get transactions`() {
        XlmDataManager(
            givenTransactions("ANY" to getResponseList()),
            mock()
        )
            .getTransactionList(AccountReference.Xlm("", "ANY"))
            .testSingle() `should equal` getXlmList()
    }

    private fun getXlmList(): List<XlmTransaction> = listOf(
        XlmTransaction(
            timeStamp = "createdAt",
            total = 10000.lumens(),
            hash = "transactionHash",
            to = HorizonKeyPair.Public("GCO724H2FOHPBFF4OQ6IB5GB3CVE4W3UGDY4RIHHG6UPQ2YZSSCINMAI"),
            from = HorizonKeyPair.Public("GAIH3ULLFQ4DGSECF2AR555KZ4KNDGEKN4AFI4SU2M7B43MGK3QJZNSR")
        ),
        XlmTransaction(
            timeStamp = "createdAt",
            total = 100.lumens(),
            hash = "transactionHash",
            to = HorizonKeyPair.Public("GBAHSNSG37BOGBS4GXUPMHZWJQ22WIOJQYORRBHTABMMU6SGSKDEAOPT"),
            from = HorizonKeyPair.Public("GC24LNYWXIYYB6OGCMAZZ5RX6WPI2F74ZV7HNBV4ADALLXJRT7ZTLHP2")
        )
    )

    private fun getResponseList(): List<OperationResponse> {
        val mockIgnored: SetOptionsOperationResponse = mock()
        val mockCreate: CreateAccountOperationResponse = mock {
            on { createdAt } `it returns` "createdAt"
            on { startingBalance } `it returns` "10000"
            on { transactionHash } `it returns` "transactionHash"
            on { account } `it returns`
                KeyPair.fromAccountId("GCO724H2FOHPBFF4OQ6IB5GB3CVE4W3UGDY4RIHHG6UPQ2YZSSCINMAI")
            on { funder } `it returns`
                KeyPair.fromAccountId("GAIH3ULLFQ4DGSECF2AR555KZ4KNDGEKN4AFI4SU2M7B43MGK3QJZNSR")
        }
        val mockPayment: PaymentOperationResponse = mock {
            on { createdAt } `it returns` "createdAt"
            on { amount } `it returns` "100"
            on { transactionHash } `it returns` "transactionHash"
            on { to } `it returns`
                KeyPair.fromAccountId("GBAHSNSG37BOGBS4GXUPMHZWJQ22WIOJQYORRBHTABMMU6SGSKDEAOPT")
            on { from } `it returns`
                KeyPair.fromAccountId("GC24LNYWXIYYB6OGCMAZZ5RX6WPI2F74ZV7HNBV4ADALLXJRT7ZTLHP2")
            on { type } `it returns` "payment"
        }

        return listOf(mockIgnored, mockCreate, mockPayment)
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

private fun givenTransactions(
    vararg transactions: Pair<String, List<OperationResponse>>
): HorizonProxy {
    val horizonProxy: HorizonProxy = mock()
    transactions
        .forEach { pair ->
            whenever(horizonProxy.getTransactionList(pair.first)) `it returns` pair.second
        }
    return horizonProxy
}

private fun givenMetaData(metaData: XlmMetaData): XlmMetaDataInitializer =
    mock {
        on { initWallet() } `it returns` Single.just(
            metaData
        )
    }
