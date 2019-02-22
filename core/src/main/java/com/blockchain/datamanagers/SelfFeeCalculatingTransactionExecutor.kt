package com.blockchain.datamanagers

import com.blockchain.datamanagers.fees.getFeeOptions
import com.blockchain.transactions.Memo
import info.blockchain.balance.AccountReference
import info.blockchain.balance.CryptoValue
import io.reactivex.Single
import piuk.blockchain.androidcore.data.fees.FeeDataManager
import timber.log.Timber

internal class SelfFeeCalculatingTransactionExecutor(
    private val transactionExecutor: TransactionExecutor,
    private val feeDataManager: FeeDataManager
) : TransactionExecutorWithoutFees, TransactionExecutorAddresses by transactionExecutor {

    override fun getFeeForTransaction(amount: CryptoValue, account: AccountReference): Single<CryptoValue> {
        return feeDataManager.getFeeOptions(amount.currency)
            .flatMap { fees ->
                transactionExecutor.getFeeForTransaction(
                    amount,
                    account,
                    fees
                )
            }
    }

    override fun executeTransaction(
        amount: CryptoValue,
        destination: String,
        sourceAccount: AccountReference,
        memo: Memo?
    ): Single<String> {
        return feeDataManager.getFeeOptions(amount.currency)
            .flatMap { fees ->
                transactionExecutor.executeTransaction(
                    amount,
                    destination,
                    sourceAccount,
                    fees,
                    memo = memo
                )
            }
    }

    override fun getMaximumSpendable(accountReference: AccountReference): Single<CryptoValue> {
        return feeDataManager.getFeeOptions(accountReference.cryptoCurrency)
            .flatMap { fees -> transactionExecutor.getMaximumSpendable(accountReference, fees) }
            .doOnError(Timber::e)
            .onErrorReturn { CryptoValue.zero(accountReference.cryptoCurrency) }
    }
}
