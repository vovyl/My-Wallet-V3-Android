package com.blockchain.accounts

import info.blockchain.balance.AccountReference
import io.reactivex.Observable
import io.reactivex.Single

internal class AsyncAllAccountListImplementation(private val subLists: List<AsyncAccountList>) : AsyncAllAccountList {

    override fun allAccounts(): Single<List<AccountReference>> =
        Observable.fromIterable(subLists)
            .flatMapSingle { it.accounts() }
            .flatMapIterable { it }
            .toList()
}
