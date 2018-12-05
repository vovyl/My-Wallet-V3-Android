package com.blockchain.accounts

import info.blockchain.balance.AccountReference
import info.blockchain.balance.CryptoCurrency
import io.reactivex.Single

interface AccountList {
    fun defaultAccountReference(): AccountReference
}

interface AllAccountList {
    operator fun get(cryptoCurrency: CryptoCurrency): AccountList
}

interface AsyncAccountList {

    fun accounts(): Single<List<AccountReference>>
}

interface AsyncAllAccountList {

    fun allAccounts(): Single<List<AccountReference>>
}
