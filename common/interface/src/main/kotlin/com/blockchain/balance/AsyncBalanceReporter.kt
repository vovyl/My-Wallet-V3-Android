package com.blockchain.balance

import info.blockchain.balance.CryptoValue
import io.reactivex.Single

interface AsyncBalanceReporter {
    fun entireBalance(): Single<CryptoValue>
    fun watchOnlyBalance(): Single<CryptoValue>
    fun importedAddressBalance(): Single<CryptoValue>
    fun addressBalance(address: String): Single<CryptoValue>
}

interface AsyncAddressBalanceReporter {

    fun getBalance(address: String): Single<CryptoValue>
}
