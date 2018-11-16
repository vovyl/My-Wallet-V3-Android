package com.blockchain.transactions

import com.blockchain.logging.CustomEventBuilder
import com.blockchain.logging.EventLogger
import io.reactivex.Single

fun TransactionSender.logMemoType(eventLogger: EventLogger): TransactionSender =
    MemoTypeLogger(this, eventLogger)

private class MemoTypeLogger(
    private val transactionSender: TransactionSender,
    private val eventLogger: EventLogger
) : TransactionSender by transactionSender {

    override fun sendFunds(sendDetails: SendDetails): Single<SendFundsResult> =
        transactionSender.sendFunds(sendDetails)
            .flatMap {
                if (it.success) {
                    (sendDetails.memo?.toCustomEvent() ?: noMemoEvent).let { eventLogger.logEvent(it) }
                }
                Single.just(it)
            }
}

private fun Memo.toCustomEvent(): CustomEventBuilder = if (this.isEmpty()) {
    noMemoEvent
} else {
    MemoTypeLog().putMemoType(this.type!!)
}

private val noMemoEvent = object : CustomEventBuilder("Memo not Used") {}

private class MemoTypeLog : CustomEventBuilder("Memo Used") {

    fun putMemoType(type: String): MemoTypeLog {
        putCustomAttribute("Type", type)
        return this
    }
}