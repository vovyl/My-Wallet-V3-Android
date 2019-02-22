package com.blockchain.transactions

import info.blockchain.balance.AccountReference
import info.blockchain.balance.CryptoValue
import io.reactivex.Single

interface TransactionSender {

    /**
     * Send funds with default fees
     */
    fun sendFunds(
        sendDetails: SendDetails
    ): Single<SendFundsResult>

    fun dryRunSendFunds(
        sendDetails: SendDetails
    ): Single<SendFundsResult>
}

/**
 * Send funds, if it fails, it throws
 */
fun TransactionSender.sendFundsOrThrow(
    sendDetails: SendDetails
): Single<SendFundsResult> =
    sendFunds(sendDetails)
        .doOnSuccess {
            if (!it.success) {
                throw SendException(it)
            }
        }

class SendException(
    result: SendFundsResult
) : RuntimeException("SendException ${result.errorCode}") {
    val errorCode = result.errorCode
    val hash = result.hash
    val details = result.sendDetails
}

data class SendDetails(
    val from: AccountReference,
    val value: CryptoValue,
    val toAddress: String,
    val memo: Memo? = null
)

data class Memo(

    val value: String,

    /**
     * This is open type for TransactionSender to interpret however it likes.
     * For example, the types of memo available to Xlm are different to those available in other currencies.
     */
    val type: String? = null
) {
    fun isEmpty() = value.isBlank()

    companion object {
        val None = Memo("", null)
    }
}

data class SendFundsResult(
    val sendDetails: SendDetails,
    /**
     * Currency Specific error code, refer to the implementation
     */
    val errorCode: Int,
    val confirmationDetails: SendConfirmationDetails?,
    val hash: String?,
    val errorValue: CryptoValue? = null
) {
    val success = errorCode == 0 && hash != null
}

interface SendFundsResultLocalizer {

    fun localize(sendFundsResult: SendFundsResult): String
}

data class SendConfirmationDetails(
    val sendDetails: SendDetails,
    val fees: CryptoValue
) {
    val from: AccountReference = sendDetails.from
    val to: String = sendDetails.toAddress
    val amount: CryptoValue = sendDetails.value
}
