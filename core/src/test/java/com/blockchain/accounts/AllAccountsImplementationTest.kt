package com.blockchain.accounts

import com.nhaarman.mockito_kotlin.mock
import info.blockchain.balance.CryptoCurrency
import org.amshove.kluent.`should be`
import org.amshove.kluent.`should throw the Exception`
import org.amshove.kluent.`with message`
import org.junit.Test

class AllAccountsImplementationTest {

    @Test
    fun `can get BTC`() {
        val btcAccountList = mock<AccountList>()
        val allAccountList: AllAccountList = AllAccountsImplementation(
            btcAccountList = btcAccountList,
            bchAccountList = mock(),
            etherAccountList = mock()
        )
        allAccountList[CryptoCurrency.BTC] `should be` btcAccountList
    }

    @Test
    fun `can get BCH`() {
        val bchAccountList = mock<AccountList>()
        val allAccountList: AllAccountList = AllAccountsImplementation(
            btcAccountList = mock(),
            bchAccountList = bchAccountList,
            etherAccountList = mock()
        )
        allAccountList[CryptoCurrency.BCH] `should be` bchAccountList
    }

    @Test
    fun `can get ETH`() {
        val ethAccountList = mock<AccountList>()
        val allAccountList: AllAccountList = AllAccountsImplementation(
            btcAccountList = mock(),
            bchAccountList = mock(),
            etherAccountList = ethAccountList
        )
        allAccountList[CryptoCurrency.ETHER] `should be` ethAccountList
    }

    @Test
    fun `can't get XLM because that requires an RX Single`() {
        val allAccountList: AllAccountList = AllAccountsImplementation(
            btcAccountList = mock(),
            bchAccountList = mock(),
            etherAccountList = mock()
        );
        {
            allAccountList[CryptoCurrency.XLM]
        } `should throw the Exception`
            IllegalArgumentException::class `with message` "XLM default account access requires RX"
    }
}
