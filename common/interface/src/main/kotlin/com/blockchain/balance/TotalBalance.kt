package com.blockchain.balance

import info.blockchain.balance.CryptoCurrency
import info.blockchain.balance.CryptoValue
import io.reactivex.Single

interface TotalBalance {

    /**
     * Returns Total balance as (Spendable, Watch Only) pair
     */
    fun balanceSpendableToWatchOnly(cryptoCurrency: CryptoCurrency): Single<Pair<CryptoValue, CryptoValue>>
}
