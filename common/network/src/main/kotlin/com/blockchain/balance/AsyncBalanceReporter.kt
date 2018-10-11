package com.blockchain.balance

import info.blockchain.balance.BalanceReporter
import info.blockchain.balance.CryptoValue
import io.reactivex.Single

interface AsyncBalanceReporter {
    fun entireBalance(): Single<CryptoValue>
    fun watchOnlyBalance(): Single<CryptoValue>
    fun importedAddressBalance(): Single<CryptoValue>
    fun addressBalance(address: String): Single<CryptoValue>
}

fun BalanceReporter.toAsync(): AsyncBalanceReporter =
    object : AsyncBalanceReporter {

        override fun entireBalance() =
            Single.just(this@toAsync.entireBalance())

        override fun watchOnlyBalance() =
            Single.just(this@toAsync.watchOnlyBalance())

        override fun importedAddressBalance() =
            Single.just(this@toAsync.importedAddressBalance())

        override fun addressBalance(address: String) =
            Single.just(this@toAsync.addressBalance(address))
    }
