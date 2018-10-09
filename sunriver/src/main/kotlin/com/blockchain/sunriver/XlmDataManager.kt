package com.blockchain.sunriver

import info.blockchain.balance.AccountReference
import info.blockchain.balance.CryptoValue
import io.reactivex.Single

class XlmDataManager internal constructor(private val horizonProxy: HorizonProxy) {

    fun getBalance(accountReference: AccountReference.Xlm): Single<CryptoValue> =
        Single.just(horizonProxy.getBalance(accountReference.accountId))
}
