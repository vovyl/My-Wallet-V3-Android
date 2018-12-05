package com.blockchain.account

import info.blockchain.balance.AccountReference
import info.blockchain.balance.CryptoValue
import io.reactivex.Single

/**
 * General Data Manager for default accounts
 */
interface DefaultAccountDataManager {

    /**
     * Balance - minimum - fees
     */
    fun getMaxSpendableAfterFees(): Single<CryptoValue>

    fun defaultAccountReference(): Single<AccountReference>

    fun getBalanceAndMin(): Single<BalanceAndMin>
}

class BalanceAndMin(
    val balance: CryptoValue,
    val minimumBalance: CryptoValue
)
