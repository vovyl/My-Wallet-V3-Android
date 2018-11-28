package com.blockchain.balance

import com.blockchain.testutils.bitcoin
import com.blockchain.testutils.bitcoinCash
import com.blockchain.testutils.ether
import com.nhaarman.mockito_kotlin.mock
import info.blockchain.api.data.Balance
import io.reactivex.Observable
import io.reactivex.Single
import org.amshove.kluent.`it returns`
import org.junit.Test

class BalanceAdapterTest {

    @Test
    fun `get btc balance`() {
        val address = "address"
        val balance = Balance().apply { finalBalance = 1.bitcoin().amount }
        BtcBalanceAdapter(
            mock {
                on { getBalanceOfAddresses(listOf(address)) } `it returns` Observable.just(
                    LinkedHashMap<String, Balance>().apply {
                        put(address, balance)
                    }
                )
            }
        ).getBalance(address)
            .test()
            .assertComplete()
            .assertNoErrors()
            .assertValue(1.bitcoin())
    }

    @Test
    fun `get bch balance`() {
        val address = "address"
        BchBalanceAdapter(
            mock {
                on { getBalance(address) } `it returns` Single.just(1.bitcoinCash().amount)
            }
        ).getBalance(address)
            .test()
            .assertComplete()
            .assertNoErrors()
            .assertValue(1.bitcoinCash())
    }

    @Test
    fun `get eth balance`() {
        val address = "address"
        EthBalanceAdapter(
            mock {
                on { getBalance(address) } `it returns` Single.just(1.ether().amount)
            }
        ).getBalance(address)
            .test()
            .assertComplete()
            .assertNoErrors()
            .assertValue(1.ether())
    }
}