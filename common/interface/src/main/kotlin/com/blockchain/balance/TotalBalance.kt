package com.blockchain.balance

import info.blockchain.balance.CryptoCurrency
import info.blockchain.balance.CryptoValue
import io.reactivex.Single

interface TotalBalance {

    class Balance(
        /**
         * The total spendable balance.
         */
        val spendable: CryptoValue,

        /**
         * The total cold storage balance.
         */
        val coldStorage: CryptoValue,

        /**
         * This is the watch only balance, a balance we do not have the keys for.
         */
        val watchOnly: CryptoValue
    ) {

        val spendableAndColdStorage = spendable + coldStorage

        companion object {

            fun zero(currency: CryptoCurrency): Balance {
                val zero = CryptoValue.zero(currency)
                return Balance(zero, zero, zero)
            }
        }
    }

    /**
     * Returns Total balance as (Spendable, ColdStorage, Watch Only) triple
     */
    fun totalBalance(cryptoCurrency: CryptoCurrency): Single<Balance>
}
