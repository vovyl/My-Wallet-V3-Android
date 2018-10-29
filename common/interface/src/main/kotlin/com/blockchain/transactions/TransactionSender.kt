package com.blockchain.transactions

import info.blockchain.balance.AccountReference
import info.blockchain.balance.CryptoValue
import io.reactivex.Single

interface TransactionSender {

    /**
     * Send funds with default fees
     */
    fun sendFunds(
        from: AccountReference,
        value: CryptoValue,
        toAddress: String
    ): Single<String>
}
