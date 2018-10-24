package com.blockchain.sunriver

import com.blockchain.sunriver.datamanager.XlmAccount
import com.blockchain.sunriver.datamanager.XlmMetaData
import com.blockchain.sunriver.datamanager.XlmMetaDataInitializer
import com.blockchain.sunriver.models.XlmTransaction
import com.blockchain.testutils.lumens
import com.blockchain.testutils.rxInit
import com.blockchain.testutils.stroops
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
import com.nhaarman.mockito_kotlin.verifyZeroInteractions
import com.nhaarman.mockito_kotlin.whenever
import info.blockchain.balance.AccountReference
import info.blockchain.balance.CryptoValue
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.amshove.kluent.`it returns`
import org.amshove.kluent.`should equal`
import org.amshove.kluent.`should throw`
import org.amshove.kluent.mock
import org.junit.Rule
import org.junit.Test
import org.stellar.sdk.KeyPair
import org.stellar.sdk.responses.TransactionResponse
import org.stellar.sdk.responses.operations.CreateAccountOperationResponse
import org.stellar.sdk.responses.operations.ManageDataOperationResponse
import org.stellar.sdk.responses.operations.OperationResponse
import org.stellar.sdk.responses.operations.PaymentOperationResponse
import org.stellar.sdk.responses.operations.SetOptionsOperationResponse

class XlmDataManagerTest {

    @get:Rule
    val initSchedulers = rxInit {
        ioTrampoline()
    }

    @Test
    fun `getBalance - there should be no interactions before subscribe`() {
        verifyNoInteractionsBeforeSubscribe {
            getBalance()
        }
    }

    @Test
    fun `getBalance with reference - there should be no interactions before subscribe`() {
        verifyNoInteractionsBeforeSubscribe {
            getBalance(AccountReference.Xlm("", "ANY"))
        }
    }

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

class XlmDataManagerTransactionListTest {

    @get:Rule
    val initSchedulers = rxInit {
        ioTrampoline()
    }

    @Test
    fun `getTransactionList - there should be no interactions before subscribe`() {
        verifyNoInteractionsBeforeSubscribe {
            getTransactionList()
        }
    }

    @Test
    fun `getTransactionList with reference - there should be no interactions before subscribe`() {
        verifyNoInteractionsBeforeSubscribe {
            getTransactionList(AccountReference.Xlm("", "ANY"))
        }
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

    @Test
    fun `map response rejects unsupported types`() {
        val unsupportedResponse: ManageDataOperationResponse = mock();
        {
            mapOperationResponse(unsupportedResponse)
        } `should throw` IllegalArgumentException::class
    }

    @Test
    fun `get transaction fee`() {
        XlmDataManager(
            givenTransaction("HASH" to getTransaction()),
            mock()
        )
            .getTransactionFee("HASH")
            .testSingle() `should equal` 100.stroops()
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

    private fun getTransaction(): TransactionResponse = mock {
        on { feePaid } `it returns` 100L
    }
}

class XlmDataManagerSendTransactionTest {

    @get:Rule
    val initSchedulers = rxInit {
        ioTrampoline()
    }

    @Test
    fun `sendFromDefault - there should be no interactions before subscribe`() {
        verifyNoInteractionsBeforeSubscribe {
            sendFromDefault(HorizonKeyPair.Public("ANY"), 100.lumens())
        }
    }

    @Test
    fun `sendFunds with reference - there should be no interactions before subscribe`() {
        verifyNoInteractionsBeforeSubscribe {
            sendFunds(
                AccountReference.Xlm("", "ANY"),
                100.lumens(),
                "ANY"
            )
        }
    }

    @Test
    fun `can send`() {
        val horizonProxy: HorizonProxy = mock {
            on {
                sendTransaction(
                    source = keyPairEq(
                        KeyPair.fromSecretSeed(
                            "SCIB3NRLJR6BPQRF3WCSPBICSZIXNLGHKWDZZ32OA6TFOJJKWGNHOHIA"
                        )
                    ),
                    destination = keyPairEq(
                        KeyPair.fromAccountId(
                            "GDKDDBJNREDV4ITL65Z3PNKAGWYJQL7FZJSV4P2UWGLRXI6AWT36UED3"
                        )
                    ),
                    amount = eq(199.456.lumens())
                )
            } `it returns` HorizonProxy.SendResult(
                success = true,
                transaction = mock()
            )
        }
        XlmDataManager(
            horizonProxy,
            givenMetaData(
                XlmMetaData(
                    defaultAccountIndex = 0,
                    accounts = listOf(
                        XlmAccount(
                            publicKey = "",
                            secret = "SCIB3NRLJR6BPQRF3WCSPBICSZIXNLGHKWDZZ32OA6TFOJJKWGNHOHIA",
                            label = "",
                            archived = false
                        )
                    ),
                    transactionNotes = emptyMap()
                )
            )
        ).sendFromDefault(
            HorizonKeyPair.createValidatedPublic("GDKDDBJNREDV4ITL65Z3PNKAGWYJQL7FZJSV4P2UWGLRXI6AWT36UED3"),
            199.456.lumens()
        ).test()
            .assertNoErrors()
            .assertComplete()
        horizonProxy.verifyJustTheOneSendAttempt()
    }

    @Test
    fun `any failure bubbles up`() {
        val horizonProxy: HorizonProxy = mock {
            on { sendTransaction(any(), any(), any()) } `it returns` HorizonProxy.SendResult(
                success = false,
                transaction = mock()
            )
        }
        XlmDataManager(
            horizonProxy,
            givenMetaData(
                XlmMetaData(
                    defaultAccountIndex = 0,
                    accounts = listOf(
                        XlmAccount(
                            publicKey = "",
                            secret = "SCIB3NRLJR6BPQRF3WCSPBICSZIXNLGHKWDZZ32OA6TFOJJKWGNHOHIA",
                            label = "",
                            archived = false
                        )
                    ),
                    transactionNotes = emptyMap()
                )
            )
        ).sendFromDefault(
            HorizonKeyPair.createValidatedPublic("GDKDDBJNREDV4ITL65Z3PNKAGWYJQL7FZJSV4P2UWGLRXI6AWT36UED3"),
            199.456.lumens()
        ).test()
            .assertFailureAndMessage(XlmSendException::class.java, "Send failed")
        horizonProxy.verifyJustTheOneSendAttempt()
    }

    @Test
    fun `can send from a specific account`() {
        val horizonProxy: HorizonProxy = mock {
            on {
                sendTransaction(
                    source = keyPairEq(
                        KeyPair.fromSecretSeed(
                            "SCIB3NRLJR6BPQRF3WCSPBICSZIXNLGHKWDZZ32OA6TFOJJKWGNHOHIA"
                        )
                    ),
                    destination = keyPairEq(
                        KeyPair.fromAccountId(
                            "GDKDDBJNREDV4ITL65Z3PNKAGWYJQL7FZJSV4P2UWGLRXI6AWT36UED3"
                        )
                    ),
                    amount = eq(1.23.lumens())
                )
            } `it returns` HorizonProxy.SendResult(
                success = true,
                transaction = mock()
            )
        }
        XlmDataManager(
            horizonProxy,
            givenMetaData(
                XlmMetaData(
                    defaultAccountIndex = 0,
                    accounts = listOf(
                        XlmAccount(
                            publicKey = "GBVO27UV2OXJFLFNXHMXOR5WRPKETM64XAQHUEKQ67W5LQDPZCDSTUTF",
                            secret = "SBGS72YDKMO7K6YBDGXSD2U7BGFK3LRDCR36KNNXVL7N7L2OSEQSWO25",
                            label = "",
                            archived = false
                        ),
                        XlmAccount(
                            publicKey = "GB5INYM5XFJHAIQYXUQMGMQEM5KWBM4OYVLTWQI5JSQBRQKFYH3M3XWR",
                            secret = "SCIB3NRLJR6BPQRF3WCSPBICSZIXNLGHKWDZZ32OA6TFOJJKWGNHOHIA",
                            label = "",
                            archived = false
                        )
                    ),
                    transactionNotes = emptyMap()
                )
            )
        ).sendFunds(
            AccountReference.Xlm("", "GB5INYM5XFJHAIQYXUQMGMQEM5KWBM4OYVLTWQI5JSQBRQKFYH3M3XWR"),
            1.23.lumens(),
            "GDKDDBJNREDV4ITL65Z3PNKAGWYJQL7FZJSV4P2UWGLRXI6AWT36UED3"
        ).test()
            .assertNoErrors()
            .assertComplete()
        horizonProxy.verifyJustTheOneSendAttempt()
    }

    @Test
    fun `when can't find the specific account in the meta data - throw`() {
        val horizonProxy = mock<HorizonProxy>()
        XlmDataManager(
            horizonProxy,
            givenMetaData(
                XlmMetaData(
                    defaultAccountIndex = 0,
                    accounts = listOf(
                        XlmAccount(
                            publicKey = "GBVO27UV2OXJFLFNXHMXOR5WRPKETM64XAQHUEKQ67W5LQDPZCDSTUTF",
                            secret = "SBGS72YDKMO7K6YBDGXSD2U7BGFK3LRDCR36KNNXVL7N7L2OSEQSWO25",
                            label = "",
                            archived = false
                        )
                    ),
                    transactionNotes = emptyMap()
                )
            )
        ).sendFunds(
            AccountReference.Xlm("", "GB5INYM5XFJHAIQYXUQMGMQEM5KWBM4OYVLTWQI5JSQBRQKFYH3M3XWR"),
            1.23.lumens(),
            "GDKDDBJNREDV4ITL65Z3PNKAGWYJQL7FZJSV4P2UWGLRXI6AWT36UED3"
        ).test()
            .assertFailureAndMessage(XlmSendException::class.java, "Account not found in meta data")
            .assertNotComplete()
        verifyZeroInteractions(horizonProxy)
    }

    @Test
    fun `when the address is not valid - throw`() {
        val horizonProxy = mock<HorizonProxy>()
        XlmDataManager(
            horizonProxy,
            mock()
        ).sendFunds(
            AccountReference.Xlm("", "GB5INYM5XFJHAIQYXUQMGMQEM5KWBM4OYVLTWQI5JSQBRQKFYH3M3XWR"),
            1.23.lumens(),
            "GDKDDBJNREDV4ITL65Z3PNKAGWYJQL7FZJSV4P2UWGLRXI6AWT36UED4"
        ).test()
            .assertFailureAndMessage(InvalidAccountIdException::class.java, "Invalid Account Id, Checksum invalid")
            .assertNotComplete()
        verifyZeroInteractions(horizonProxy)
    }

    @Test
    fun `when the from address reference is not an Xlm one - throw`() {
        val horizonProxy = mock<HorizonProxy>()
        XlmDataManager(
            horizonProxy,
            mock()
        ).sendFunds(
            AccountReference.Ethereum("", "0xAddress"),
            1.23.lumens(),
            "GDKDDBJNREDV4ITL65Z3PNKAGWYJQL7FZJSV4P2UWGLRXI6AWT36UED3"
        ).test()
            .assertFailureAndMessage(XlmSendException::class.java, "Source account reference is not an Xlm reference")
            .assertNotComplete()
        verifyZeroInteractions(horizonProxy)
    }

    private fun HorizonProxy.verifyJustTheOneSendAttempt() {
        verify(this).sendTransaction(any(), any(), any())
        verifyNoMoreInteractions(this)
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

private fun givenTransaction(
    vararg transactions: Pair<String, TransactionResponse>
): HorizonProxy {
    val horizonProxy: HorizonProxy = mock()
    transactions
        .forEach { pair ->
            whenever(horizonProxy.getTransaction(pair.first)) `it returns` pair.second
        }
    return horizonProxy
}

private fun givenMetaData(metaData: XlmMetaData): XlmMetaDataInitializer =
    mock {
        on { initWallet() } `it returns` Single.just(
            metaData
        ).subscribeOn(Schedulers.io())
    }

private fun verifyNoInteractionsBeforeSubscribe(function: XlmDataManager.() -> Unit) {
    val horizonProxy = mock<HorizonProxy>()
    val metaDataInitializer = mock<XlmMetaDataInitializer>()
    val xlmDataManager = XlmDataManager(
        horizonProxy,
        metaDataInitializer
    )
    function(xlmDataManager)
    verifyZeroInteractions(horizonProxy)
    verifyZeroInteractions(metaDataInitializer)
}
