package com.blockchain.sunriver

import com.blockchain.sunriver.datamanager.XlmMetaDataInitializer
import info.blockchain.balance.AccountReference
import info.blockchain.balance.CryptoValue
import io.reactivex.Single

class XlmDataManager internal constructor(
    private val horizonProxy: HorizonProxy,
    private val metaDataInitializer: XlmMetaDataInitializer
) {

    fun getBalance(accountReference: AccountReference.Xlm): Single<CryptoValue> =
        Single.just(horizonProxy.getBalance(accountReference.accountId))

    fun getBalance(): Single<CryptoValue> =
        metaDataInitializer.initWallet("Alan's Xlm wallet")
            .map { it.accounts!![it.defaultAccountIndex] }
            .flatMap { getBalance(AccountReference.Xlm(it.label ?: "", it.publicKey)) }
}
