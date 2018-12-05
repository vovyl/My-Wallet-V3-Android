package com.blockchain.transactions

import com.blockchain.logging.LastTxUpdater
import com.blockchain.testutils.lumens
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import info.blockchain.balance.AccountReference
import io.reactivex.Completable
import io.reactivex.Single
import org.amshove.kluent.`it returns`
import org.amshove.kluent.`should equal`
import org.junit.Test

class TransactionSenderLoggerTest {

    @Test
    fun `on success, update last tx`() {
        val lastTxUpdater: LastTxUpdater = mock {
            on { updateLastTxTime() } `it returns` Completable.complete()
        }
        val sendDetails = givenSendDetails()
        val result = SendFundsResult(sendDetails, 0, null, "HASH")
        val transactionSender: TransactionSender = mock {
            on { sendFunds(sendDetails) } `it returns` Single.just(
                result
            )
        }
        transactionSender
            .updateLastTxOnSend(lastTxUpdater)
            .sendFunds(
                sendDetails
            ).test().assertComplete()
            .values().single() `should equal` result
        verify(lastTxUpdater).updateLastTxTime()
    }

    @Test
    fun `on non-success, do not update last tx`() {
        val lastTxUpdater: LastTxUpdater = mock {
            on { updateLastTxTime() } `it returns` Completable.complete()
        }
        val sendDetails = givenSendDetails()
        val result = SendFundsResult(sendDetails, 1, null, null)
        val transactionSender: TransactionSender = mock {
            on { sendFunds(sendDetails) } `it returns` Single.just(
                result
            )
        }
        transactionSender
            .updateLastTxOnSend(lastTxUpdater)
            .sendFunds(
                sendDetails
            ).test().assertComplete()
            .values().single() `should equal` result
        verify(lastTxUpdater, never()).updateLastTxTime()
    }

    @Test
    fun `on last tx failure still return result`() {
        val lastTxUpdater: LastTxUpdater = mock {
            on { updateLastTxTime() } `it returns` Completable.error(Exception())
        }
        val sendDetails = givenSendDetails()
        val result = SendFundsResult(sendDetails, 0, null, "HASH")
        val transactionSender: TransactionSender = mock {
            on { sendFunds(sendDetails) } `it returns` Single.just(
                result
            )
        }
        transactionSender
            .updateLastTxOnSend(lastTxUpdater)
            .sendFunds(
                sendDetails
            ).test().assertComplete()
            .values().single() `should equal` result
        verify(lastTxUpdater).updateLastTxTime()
    }

    private fun givenSendDetails() =
        SendDetails(
            from = AccountReference.Xlm("", "GABC"),
            toAddress = "GDEF",
            value = 100.lumens()
        )
}
