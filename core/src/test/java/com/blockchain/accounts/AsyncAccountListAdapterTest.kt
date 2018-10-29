package com.blockchain.accounts

import com.nhaarman.mockito_kotlin.mock
import info.blockchain.balance.AccountReference
import info.blockchain.balance.CryptoCurrency
import info.blockchain.wallet.coin.GenericMetadataAccount
import info.blockchain.wallet.payload.data.Account
import org.amshove.kluent.`it returns`
import org.amshove.kluent.`should equal`
import org.junit.Test

class AsyncAccountListAdapterTest {

    @Test
    fun `BtcAsyncAccountListAdapter account list`() {
        (BtcAsyncAccountListAdapter(mock {
            on { this.accounts } `it returns` listOf(Account().apply {
                label = "Account 1"
                xpub = "xpub 1"
            },
                Account().apply {
                    label = "Account 2"
                    xpub = "xpub 2"
                })
        }) as AsyncAccountList)
            .testAccountList() `should equal`
            listOf(
                AccountReference.BitcoinLike(CryptoCurrency.BTC, "Account 1", xpub = "xpub 1"),
                AccountReference.BitcoinLike(CryptoCurrency.BTC, "Account 2", xpub = "xpub 2")
            )
    }

    @Test
    fun `BchAsyncAccountListAdapter account list`() {
        (BchAsyncAccountListAdapter(mock {
            on { this.getAccountMetadataList() } `it returns` listOf(
                GenericMetadataAccount().apply {
                    label = "The first bch account"
                    xpub = "xpub 1"
                },
                GenericMetadataAccount().apply {
                    label = "The second bch account"
                    xpub = "xpub 2"
                }
            )
        }) as AsyncAccountList)
            .testAccountList() `should equal`
            listOf(
                AccountReference.BitcoinLike(CryptoCurrency.BCH, "The first bch account", xpub = "xpub 1"),
                AccountReference.BitcoinLike(CryptoCurrency.BCH, "The second bch account", xpub = "xpub 2")
            )
    }

    @Test
    fun `EtherAsyncAccountListAdapter account list`() {
        val accountReference = AccountReference.Ethereum("Ether Account", "0xAddress")
        (EthAsyncAccountListAdapter(mock {
            on { defaultAccountReference() } `it returns`
                accountReference
        }) as AsyncAccountList)
            .testAccountList() `should equal`
            listOf(
                accountReference
            )
    }
}

private fun AsyncAccountList.testAccountList(): List<AccountReference> =
    accounts()
        .test()
        .assertComplete()
        .values()
        .single()
