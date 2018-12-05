package com.blockchain.transactions

import com.blockchain.logging.EventLogger
import com.blockchain.testutils.lumens
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import info.blockchain.balance.AccountReference
import io.reactivex.Single
import org.amshove.kluent.`it returns`
import org.amshove.kluent.`should equal`
import org.amshove.kluent.any
import org.junit.Test

class MemoTypeLoggerTest {

    @Test
    fun `on success, log memo type`() {
        val eventLogger: EventLogger = mock()
        val sendDetails = givenSendDetails(Memo("test", "text"))
        val result = SendFundsResult(sendDetails, 0, null, "HASH")
        val transactionSender: TransactionSender = mock {
            on { sendFunds(sendDetails) } `it returns` Single.just(result)
        }
        transactionSender
            .logMemoType(eventLogger)
            .sendFunds(sendDetails)
            .test()
            .assertComplete()
            .values().single() `should equal` result
        verify(eventLogger).logEvent(any())
    }

    @Test
    fun `on success but memo type empty, should log`() {
        val eventLogger: EventLogger = mock()
        val sendDetails = givenSendDetails(Memo.None)
        val result = SendFundsResult(sendDetails, 0, null, "HASH")
        val transactionSender: TransactionSender = mock {
            on { sendFunds(sendDetails) } `it returns` Single.just(result)
        }
        transactionSender
            .logMemoType(eventLogger)
            .sendFunds(sendDetails).test()
            .assertComplete()
            .values()
            .single() `should equal` result
        verify(eventLogger).logEvent(any())
    }

    @Test
    fun `on success but no memo, should log`() {
        val eventLogger: EventLogger = mock()
        val sendDetails = givenSendDetails()
        val result = SendFundsResult(sendDetails, 0, null, "HASH")
        val transactionSender: TransactionSender = mock {
            on { sendFunds(sendDetails) } `it returns` Single.just(result)
        }
        transactionSender
            .logMemoType(eventLogger)
            .sendFunds(sendDetails).test()
            .assertComplete()
            .values()
            .single() `should equal` result
        verify(eventLogger).logEvent(any())
    }

    @Test
    fun `on fail, no memo type logged`() {
        val eventLogger: EventLogger = mock()
        val sendDetails = givenSendDetails(Memo("test", "text"))
        val transactionSender: TransactionSender = mock {
            on { sendFunds(sendDetails) } `it returns` Single.error(Exception())
        }
        transactionSender
            .logMemoType(eventLogger)
            .sendFunds(sendDetails)
            .test()
            .assertNotComplete()
            .assertError(Exception::class.java)
        verify(eventLogger, never()).logEvent(any())
    }

    private fun givenSendDetails(memo: Memo? = null) =
        SendDetails(
            from = AccountReference.Xlm("", "GABC"),
            toAddress = "GDEF",
            value = 100.lumens(),
            memo = memo
        )
}