package com.blockchain.sunriver

import com.blockchain.testutils.lumens
import com.blockchain.testutils.stroops
import com.blockchain.transactions.SendConfirmationDetails
import com.blockchain.transactions.SendDetails
import com.blockchain.transactions.SendFundsResult
import com.nhaarman.mockito_kotlin.mock
import info.blockchain.balance.AccountReference
import org.amshove.kluent.`it returns`
import org.amshove.kluent.`should equal`
import org.junit.Test

class MapToSendFundsResultTest {

    @Test
    fun `successful send mapping`() {
        val from = AccountReference.Xlm("", "")
        val sendDetails = SendDetails(from = from, toAddress = "GABC", value = 200.lumens())
        HorizonProxy.SendResult(
            success = true,
            transaction = mock {
                on { fee } `it returns` 98
                on { hash() } `it returns` byteArrayOf(1, 2, 3)
            },
            failureReason = HorizonProxy.FailureReason.Unknown
        ).mapToSendFundsResult(sendDetails) `should equal`
            SendFundsResult(
                sendDetails = sendDetails,
                errorCode = 0,
                confirmationDetails = SendConfirmationDetails(
                    sendDetails = sendDetails,
                    fees = 98.stroops()
                ),
                hash = "010203"
            )
    }

    @Test
    fun `unsuccessful unknown send mapping`() {
        assertFailureReasonMapsToExpectedCode(HorizonProxy.FailureReason.Unknown, 1)
    }

    @Test
    fun `below minimum send mapping`() {
        assertFailureReasonMapsToExpectedCode(HorizonProxy.FailureReason.BelowMinimumSend, 2)
    }

    @Test
    fun `below minimum send for new account mapping`() {
        assertFailureReasonMapsToExpectedCode(HorizonProxy.FailureReason.BelowMinimumBalanceForNewAccount, 3)
    }

    @Test
    fun `insufficient funds mapping`() {
        assertFailureReasonMapsToExpectedCode(HorizonProxy.FailureReason.InsufficientFunds, 4)
    }

    private fun assertFailureReasonMapsToExpectedCode(
        failureReason: HorizonProxy.FailureReason,
        expectedErrorCode: Int
    ) {
        val fromAccount = AccountReference.Xlm("AC", "GEFD")
        val sendDetails = SendDetails(from = fromAccount, toAddress = "GABD", value = 50.lumens())
        HorizonProxy.SendResult(
            success = false,
            transaction = mock {
                on { fee } `it returns` 98
                on { hash() } `it returns` byteArrayOf(1, 2, 3)
            },
            failureReason = failureReason,
            failureValue = 100.lumens()
        ).mapToSendFundsResult(sendDetails) `should equal`
            SendFundsResult(
                sendDetails = sendDetails,
                errorCode = expectedErrorCode,
                confirmationDetails = null,
                hash = null,
                errorValue = 100.lumens()
            )
    }
}
