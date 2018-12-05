package com.blockchain.transactions

import com.blockchain.logging.LastTxUpdater
import io.reactivex.Single

fun TransactionSender.updateLastTxOnSend(lastTxUpdater: LastTxUpdater): TransactionSender =
    TransactionSenderLogger(this, lastTxUpdater)

private class TransactionSenderLogger(
    private val transactionSender: TransactionSender,
    private val lastTxUpdater: LastTxUpdater
) : TransactionSender by transactionSender {

    override fun sendFunds(sendDetails: SendDetails): Single<SendFundsResult> =
        transactionSender.sendFunds(sendDetails)
            .flatMap {
                if (it.success) {
                    lastTxUpdater.updateLastTxTime().onErrorComplete().toSingleDefault(it)
                } else {
                    Single.just(it)
                }
            }
}
