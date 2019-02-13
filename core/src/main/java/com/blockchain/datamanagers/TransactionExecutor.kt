package com.blockchain.datamanagers

import com.blockchain.datamanagers.fees.FeeType
import com.blockchain.datamanagers.fees.NetworkFees
import com.blockchain.transactions.Memo
import info.blockchain.balance.AccountReference
import info.blockchain.balance.CryptoValue
import io.reactivex.Single

interface TransactionExecutorAddresses {

    fun getChangeAddress(
        accountReference: AccountReference
    ): Single<String>

    fun getReceiveAddress(
        accountReference: AccountReference
    ): Single<String>
}

interface TransactionExecutor : TransactionExecutorAddresses {

    fun getFeeForTransaction(
        amount: CryptoValue,
        sourceAccount: AccountReference,
        fees: NetworkFees,
        feeType: FeeType = FeeType.Regular
    ): Single<CryptoValue>

    fun executeTransaction(
        amount: CryptoValue,
        destination: String,
        sourceAccount: AccountReference,
        fees: NetworkFees,
        feeType: FeeType = FeeType.Regular,
        memo: Memo? = null
    ): Single<String>

    fun getMaximumSpendable(
        account: AccountReference,
        fees: NetworkFees,
        feeType: FeeType = FeeType.Regular
    ): Single<CryptoValue>
}

interface MaximumSpendableCalculator {

    fun getMaximumSpendable(accountReference: AccountReference): Single<CryptoValue>
}

interface TransactionExecutorWithoutFees : TransactionExecutorAddresses, MaximumSpendableCalculator {

    fun getFeeForTransaction(
        amount: CryptoValue,
        account: AccountReference
    ): Single<CryptoValue>

    fun executeTransaction(
        amount: CryptoValue,
        destination: String,
        sourceAccount: AccountReference,
        memo: Memo? = null
    ): Single<String>
}
