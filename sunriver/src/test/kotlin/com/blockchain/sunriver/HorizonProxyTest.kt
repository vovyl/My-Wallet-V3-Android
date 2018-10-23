package com.blockchain.sunriver

import com.blockchain.koin.sunriverModule
import com.blockchain.network.initRule
import com.blockchain.testutils.after
import com.blockchain.testutils.before
import com.blockchain.testutils.bitcoin
import com.blockchain.testutils.getStringFromResource
import com.blockchain.testutils.lumens
import com.blockchain.testutils.stroops
import info.blockchain.balance.CryptoCurrency
import info.blockchain.balance.CryptoValue
import info.blockchain.balance.withMajorValue
import io.fabric8.mockwebserver.DefaultMockServer
import org.amshove.kluent.`should be instance of`
import org.amshove.kluent.`should be less than`
import org.amshove.kluent.`should be`
import org.amshove.kluent.`should equal`
import org.amshove.kluent.`should not be`
import org.amshove.kluent.`should throw`
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.standalone.StandAloneContext
import org.koin.standalone.get
import org.koin.test.AutoCloseKoinTest
import org.stellar.sdk.CreateAccountOperation
import org.stellar.sdk.KeyPair
import org.stellar.sdk.Network
import org.stellar.sdk.PaymentOperation
import org.stellar.sdk.requests.ErrorResponse
import org.stellar.sdk.responses.operations.CreateAccountOperationResponse
import org.stellar.sdk.responses.operations.PaymentOperationResponse
import java.util.Locale

class HorizonProxyTest : AutoCloseKoinTest() {

    private val server = DefaultMockServer()

    @get:Rule
    private val initMockServer = server.initRule()

    @get:Rule
    private val ensureNoLocalization = before {
        Locale.setDefault(Locale.FRANCE)
    } after {
        Locale.setDefault(Locale.US)
    }

    @Before
    fun startKoin() {
        StandAloneContext.startKoin(
            listOf(
                sunriverModule
            ),
            extraProperties = mapOf("HorizonURL" to server.url(""))
        )
    }

    @Test
    fun `get xlm balance`() {
        server.expect().get().withPath("/accounts/GC7GSOOQCBBWNUOB6DIWNVM7537UKQ353H6LCU3DB54NUTVFR2T6OHF4")
            .andReturn(
                200,
                getStringFromResource("accounts/GC7GSOOQCBBWNUOB6DIWNVM7537UKQ353H6LCU3DB54NUTVFR2T6OHF4.json")
            )
            .once()

        val proxy = get<HorizonProxy>()

        val balance =
            proxy.getBalance("GC7GSOOQCBBWNUOB6DIWNVM7537UKQ353H6LCU3DB54NUTVFR2T6OHF4")

        balance `should equal` 109969.99997.lumens()
    }

    @Test
    fun `get balance if account does not exist`() {
        server.expect().get().withPath("/accounts/GC7GSOOQCBBWNUOB6DIWNVM7537UKQ353H6LCU3DB54NUTVFR2T6OHF4")
            .andReturn(
                404,
                getStringFromResource("accounts/not_found.json")
            )
            .once()

        val proxy = get<HorizonProxy>()

        val balance =
            proxy.getBalance("GC7GSOOQCBBWNUOB6DIWNVM7537UKQ353H6LCU3DB54NUTVFR2T6OHF4")

        balance `should equal` 0.lumens()
    }

    @Test
    fun `on any other kind of server error, bubble up exception`() {
        server.expect().get().withPath("/accounts/GC7GSOOQCBBWNUOB6DIWNVM7537UKQ353H6LCU3DB54NUTVFR2T6OHF4")
            .andReturn(
                301,
                getStringFromResource("accounts/not_found.json")
            )
            .once()

        val proxy = get<HorizonProxy>();

        {
            proxy.getBalance("GC7GSOOQCBBWNUOB6DIWNVM7537UKQ353H6LCU3DB54NUTVFR2T6OHF4")
        } `should throw` ErrorResponse::class
    }

    @Test
    fun `get xlm transaction history`() {
        server.expect().get().withPath("/accounts/GC7GSOOQCBBWNUOB6DIWNVM7537UKQ353H6LCU3DB54NUTVFR2T6OHF4/operations")
            .andReturn(
                200,
                getStringFromResource("transactions/transaction_list.json")
            )
            .once()

        val proxy = get<HorizonProxy>()

        val transactions =
            proxy.getTransactionList("GC7GSOOQCBBWNUOB6DIWNVM7537UKQ353H6LCU3DB54NUTVFR2T6OHF4")

        transactions.size `should equal` 3
        transactions[0] `should be instance of` CreateAccountOperationResponse::class.java
        transactions[1] `should be instance of` PaymentOperationResponse::class.java
        transactions[2] `should be instance of` PaymentOperationResponse::class.java
    }

    @Test
    fun `get xlm transaction history if not found`() {
        server.expect().get().withPath("/accounts/GC7GSOOQCBBWNUOB6DIWNVM7537UKQ353H6LCU3DB54NUTVFR2T6OHF4/operations")
            .andReturn(
                404,
                getStringFromResource("accounts/not_found.json")
            )
            .once()

        val proxy = get<HorizonProxy>()

        val transactions =
            proxy.getTransactionList("GC7GSOOQCBBWNUOB6DIWNVM7537UKQ353H6LCU3DB54NUTVFR2T6OHF4")

        transactions.size `should equal` 0
    }

    @Test
    fun `get xlm transaction history, on any other kind of server error, bubble up exception`() {
        server.expect().get().withPath("/accounts/GC7GSOOQCBBWNUOB6DIWNVM7537UKQ353H6LCU3DB54NUTVFR2T6OHF4/operations")
            .andReturn(
                301,
                getStringFromResource("accounts/not_found.json")
            )
            .once()

        val proxy = get<HorizonProxy>();

        {
            proxy.getTransactionList("GC7GSOOQCBBWNUOB6DIWNVM7537UKQ353H6LCU3DB54NUTVFR2T6OHF4")
        } `should throw` ErrorResponse::class
    }

    @Test
    fun `accountExists - get account existence`() {
        server.givenAccountExists("GC7GSOOQCBBWNUOB6DIWNVM7537UKQ353H6LCU3DB54NUTVFR2T6OHF4")

        val proxy = get<HorizonProxy>()

        proxy.accountExists("GC7GSOOQCBBWNUOB6DIWNVM7537UKQ353H6LCU3DB54NUTVFR2T6OHF4") `should be` true
    }

    @Test
    fun `accountExists - get account non-existence`() {
        server.givenAccountDoesNotExist("GC7GSOOQCBBWNUOB6DIWNVM7537UKQ353H6LCU3DB54NUTVFR2T6OHF4")

        val proxy = get<HorizonProxy>()

        proxy.accountExists("GC7GSOOQCBBWNUOB6DIWNVM7537UKQ353H6LCU3DB54NUTVFR2T6OHF4") `should be` false
    }

    @Test
    fun `accountExists - on any other kind of server error, bubble up exception`() {
        server.expect().get().withPath("/accounts/GC7GSOOQCBBWNUOB6DIWNVM7537UKQ353H6LCU3DB54NUTVFR2T6OHF4")
            .andReturn(
                301,
                getStringFromResource("accounts/not_found.json")
            )
            .once()

        val proxy = get<HorizonProxy>();

        {
            proxy.accountExists("GC7GSOOQCBBWNUOB6DIWNVM7537UKQ353H6LCU3DB54NUTVFR2T6OHF4")
        } `should throw` ErrorResponse::class
    }

    @Test
    fun `Uses test net if url contains the word test`() {
        HorizonProxy("test_net")

        Network.current().networkPassphrase `should equal` "Test SDF Network ; September 2015"
    }

    @Test
    fun `Uses the public network if url does not contains the word test`() {
        HorizonProxy("te_st_net")

        Network.current().networkPassphrase `should equal` "Public Global Stellar Network ; September 2015"
    }

    @Test
    fun `can send transaction to an account that exists`() {
        server.givenAccountExists("GC7GSOOQCBBWNUOB6DIWNVM7537UKQ353H6LCU3DB54NUTVFR2T6OHF4")
        server.givenAccountExists("GCO724H2FOHPBFF4OQ6IB5GB3CVE4W3UGDY4RIHHG6UPQ2YZSSCINMAI")

        server.expect().post().withPath("/transactions")
            .andReturn(
                200,
                getStringFromResource("transactions/post_success.json")
            )
            .once()

        val proxy = get<HorizonProxy>()

        val source = KeyPair.fromSecretSeed("SAD6LOTFMPIGAPOF2SPQSYD4OIGIE5XVVX3FW3K7QVFUTRSUUHMZQ76I")
        source.accountId `should equal` "GC7GSOOQCBBWNUOB6DIWNVM7537UKQ353H6LCU3DB54NUTVFR2T6OHF4"

        proxy.sendTransaction(
            source,
            KeyPair.fromAccountId("GCO724H2FOHPBFF4OQ6IB5GB3CVE4W3UGDY4RIHHG6UPQ2YZSSCINMAI"),
            123.4567891.lumens()
        ).apply {
            success `should be` true
            success `should be` true
            transaction `should not be` null
            transaction!!.operations.single().apply {
                this `should be instance of` PaymentOperation::class
                (this as PaymentOperation).apply {
                    destination.accountId `should equal` "GCO724H2FOHPBFF4OQ6IB5GB3CVE4W3UGDY4RIHHG6UPQ2YZSSCINMAI"
                    amount `should equal` "123.4567891"
                }
            }
            transaction.fee `should equal` 100
        }

        server.requestCount `should be` 3
    }

    private val fee = 100.stroops()
    private val minimumBalance = 1.lumens()

    @Test
    fun `insufficient funds that we know about before transaction send - whole balance`() {
        assertFailsAndTransactionIsNotSentToHorizon(
            value = 109969.999970.lumens(),
            expectedReason = HorizonProxy.FailureReason.InsufficientFunds
        )
    }

    @Test
    fun `insufficient funds that we know about before transaction send - whole balance - fee`() {
        assertFailsAndTransactionIsNotSentToHorizon(
            value = 109969.999970.lumens() - fee,
            expectedReason = HorizonProxy.FailureReason.InsufficientFunds
        )
    }

    @Test
    fun `insufficient funds by 1 stoop`() {
        assertFailsAndTransactionIsNotSentToHorizon(
            value = 109969.999970.lumens() - fee - minimumBalance + 1.stroops(),
            expectedReason = HorizonProxy.FailureReason.InsufficientFunds
        )
    }

    @Test
    fun `absolute minimum balance can be sent`() {
        assertSendPasses(109969.999970.lumens() - fee - minimumBalance)
    }

    @Test
    fun `existing account - minimum send`() {
        assertSendPasses(minimumBalance, destinationAccountExists = true)
    }

    @Test
    fun `non-existing account - minimum send`() {
        assertSendPasses(minimumBalance, destinationAccountExists = false)
    }

    @Test
    fun `existing account - no lower limit on send`() {
        assertSendPasses(1.stroops(), destinationAccountExists = true)
    }

    @Test
    fun `existing account - can't send zero`() {
        assertFailsAndTransactionIsNotSentToHorizon(
            value = 0.lumens(),
            destinationAccountExists = true,
            expectedReason = HorizonProxy.FailureReason.BelowMinimumSend
        )
        server.assertNoInteractions()
    }

    @Test
    fun `existing account - can't send negative`() {
        assertFailsAndTransactionIsNotSentToHorizon(
            value = (-1).stroops(),
            destinationAccountExists = true,
            expectedReason = HorizonProxy.FailureReason.BelowMinimumSend
        )
        server.assertNoInteractions()
    }

    @Test
    fun `non-existing account - can't send zero`() {
        assertFailsAndTransactionIsNotSentToHorizon(
            value = 0.lumens(),
            destinationAccountExists = false,
            expectedReason = HorizonProxy.FailureReason.BelowMinimumSend
        )
        server.assertNoInteractions()
    }

    @Test
    fun `non-existing account - can't send negative`() {
        assertFailsAndTransactionIsNotSentToHorizon(
            value = (-1).stroops(),
            destinationAccountExists = false,
            expectedReason = HorizonProxy.FailureReason.BelowMinimumSend
        )
        server.assertNoInteractions()
    }

    @Test
    fun `non-existing account - minimum send less 1 stroop fails`() {
        assertFailsAndTransactionIsNotSentToHorizon(
            value = minimumBalance - 1.stroops(),
            destinationAccountExists = false,
            expectedReason = HorizonProxy.FailureReason.BelowMinimumBalanceForNewAccount
        )
        server.requestCount `should be` 1
    }

    @Test
    fun `can't send non-lumen amount`() {
        {
            assertFailsAndTransactionIsNotSentToHorizon(100.bitcoin())
        } `should throw` IllegalArgumentException::class
        server.assertNoInteractions()
    }

    /**
     * Ensures that the request is never sent to Horizon as that would cost the fee
     */
    private fun assertFailsAndTransactionIsNotSentToHorizon(
        value: CryptoValue,
        destinationAccountExists: Boolean = true,
        expectedReason: HorizonProxy.FailureReason? = null
    ) {
        val destinationAccountId = "GCO724H2FOHPBFF4OQ6IB5GB3CVE4W3UGDY4RIHHG6UPQ2YZSSCINMAI"
        server.givenAccounts(
            sourceAccountId = "GC7GSOOQCBBWNUOB6DIWNVM7537UKQ353H6LCU3DB54NUTVFR2T6OHF4",
            destinationAccountId = destinationAccountId,
            destinationAccountExists = destinationAccountExists
        )

        val proxy = get<HorizonProxy>()

        proxy.sendTransaction(
            KeyPair.fromSecretSeed("SAD6LOTFMPIGAPOF2SPQSYD4OIGIE5XVVX3FW3K7QVFUTRSUUHMZQ76I"),
            KeyPair.fromAccountId(destinationAccountId),
            value
        ).apply {
            success `should be` false
            transaction `should be` null
            if (expectedReason != null) {
                failureReason `should be` expectedReason
            }
        }

        server.requestCount `should be less than` 3
    }

    private fun assertSendPasses(
        value: CryptoValue,
        destinationAccountExists: Boolean = true
    ) {
        val destinationAccountId = "GCO724H2FOHPBFF4OQ6IB5GB3CVE4W3UGDY4RIHHG6UPQ2YZSSCINMAI"
        server.givenAccounts(
            sourceAccountId = "GC7GSOOQCBBWNUOB6DIWNVM7537UKQ353H6LCU3DB54NUTVFR2T6OHF4",
            destinationAccountId = destinationAccountId,
            destinationAccountExists = destinationAccountExists
        )

        server.expect().post().withPath("/transactions")
            .andReturn(
                200,
                getStringFromResource("transactions/post_success.json")
            )
            .once()

        val proxy = get<HorizonProxy>()

        proxy.sendTransaction(
            KeyPair.fromSecretSeed("SAD6LOTFMPIGAPOF2SPQSYD4OIGIE5XVVX3FW3K7QVFUTRSUUHMZQ76I"),
            KeyPair.fromAccountId("GCO724H2FOHPBFF4OQ6IB5GB3CVE4W3UGDY4RIHHG6UPQ2YZSSCINMAI"),
            value
        ).apply {
            success `should be` true
            transaction?.signatures?.size `should be` 1
        }

        server.requestCount `should be` 3
    }

    @Test
    fun `insufficient funds during transaction send`() {
        server.givenAccountExists("GC7GSOOQCBBWNUOB6DIWNVM7537UKQ353H6LCU3DB54NUTVFR2T6OHF4")
        server.givenAccountExists("GCO724H2FOHPBFF4OQ6IB5GB3CVE4W3UGDY4RIHHG6UPQ2YZSSCINMAI")

        server.expect().post().withPath("/transactions")
            .andReturn(
                400,
                getStringFromResource("transactions/post_fail_insufficient_funds.json")
            )
            .once()

        val proxy = get<HorizonProxy>()

        proxy.sendTransaction(
            KeyPair.fromSecretSeed("SAD6LOTFMPIGAPOF2SPQSYD4OIGIE5XVVX3FW3K7QVFUTRSUUHMZQ76I"),
            KeyPair.fromAccountId("GCO724H2FOHPBFF4OQ6IB5GB3CVE4W3UGDY4RIHHG6UPQ2YZSSCINMAI"),
            123.456789.lumens()
        ).success `should be` false

        server.requestCount `should be` 3
    }

    @Test
    fun `if destination account does not exist, it will do a create operation`() {
        server.givenAccountExists("GC7GSOOQCBBWNUOB6DIWNVM7537UKQ353H6LCU3DB54NUTVFR2T6OHF4")
        server.givenAccountDoesNotExist("GCO724H2FOHPBFF4OQ6IB5GB3CVE4W3UGDY4RIHHG6UPQ2YZSSCINMAI")

        server.expect().post().withPath("/transactions")
            .andReturn(
                200,
                getStringFromResource("transactions/post_success.json")
            )
            .once()

        val proxy = get<HorizonProxy>()

        val source = KeyPair.fromSecretSeed("SAD6LOTFMPIGAPOF2SPQSYD4OIGIE5XVVX3FW3K7QVFUTRSUUHMZQ76I")
        source.accountId `should equal` "GC7GSOOQCBBWNUOB6DIWNVM7537UKQ353H6LCU3DB54NUTVFR2T6OHF4"

        proxy.sendTransaction(
            source,
            KeyPair.fromAccountId("GCO724H2FOHPBFF4OQ6IB5GB3CVE4W3UGDY4RIHHG6UPQ2YZSSCINMAI"),
            CryptoCurrency.XLM.withMajorValue("1.23E+4".toBigDecimal())
        ).apply {
            success `should be` true
            transaction `should not be` null
            transaction!!.operations.single().apply {
                this `should be instance of` CreateAccountOperation::class
                (this as CreateAccountOperation).apply {
                    destination.accountId `should equal` "GCO724H2FOHPBFF4OQ6IB5GB3CVE4W3UGDY4RIHHG6UPQ2YZSSCINMAI"
                    startingBalance `should equal` "12300.0000000"
                }
            }
            transaction.fee `should equal` 100
        }

        server.requestCount `should be` 3
    }
}

private fun DefaultMockServer.assertNoInteractions() {
    requestCount `should be` 0
}

private fun DefaultMockServer.givenAccounts(
    sourceAccountId: String,
    destinationAccountId: String,
    destinationAccountExists: Boolean
) {
    givenAccountExists(sourceAccountId)
    if (destinationAccountExists) {
        givenAccountExists(destinationAccountId)
    } else {
        givenAccountDoesNotExist(destinationAccountId)
    }
}

private fun DefaultMockServer.givenAccountExists(accountId: String) {
    expect().get().withPath("/accounts/$accountId")
        .andReturn(
            200,
            getStringFromResource("accounts/$accountId.json")
        )
        .once()
}

private fun DefaultMockServer.givenAccountDoesNotExist(accountId: String) {
    expect().get().withPath("/accounts/$accountId")
        .andReturn(
            404,
            getStringFromResource("accounts/not_found.json")
        )
        .once()
}
