package com.blockchain.datamanagers

import com.nhaarman.mockito_kotlin.mock
import info.blockchain.balance.AccountReference
import info.blockchain.balance.CryptoCurrency
import info.blockchain.wallet.coin.GenericMetadataAccount
import info.blockchain.wallet.ethereum.EthereumAccount
import info.blockchain.wallet.payload.data.Account
import io.reactivex.Observable
import org.amshove.kluent.`it returns`
import org.amshove.kluent.`should equal`
import org.junit.Test
import piuk.blockchain.androidcore.data.bitcoincash.BchDataManager
import piuk.blockchain.androidcore.data.payload.PayloadDataManager

class AddressResolverTest {

    @Test
    fun `resolve BTC addresses test`() {
        val account = Account()
        val reference = AccountReference.BitcoinLike(CryptoCurrency.BTC, "", "XPUB")
        val accountLookup: AccountLookup = mock {
            on { getAccountFromAddressOrXPub(reference) } `it returns` account
        }
        val payloadDataManager = mock<PayloadDataManager> {
            on { getNextReceiveAddress(account) } `it returns` Observable.just("RECEIVE1")
            on { getNextChangeAddress(account) } `it returns` Observable.just("CHANGE1")
        }
        AddressResolver(accountLookup, payloadDataManager, mock())
            .addressPairForAccount(reference)
            .test().values().single()
            .apply {
                receivingAddress `should equal` "RECEIVE1"
                changeAddress `should equal` "CHANGE1"
            }
        AddressResolver(accountLookup, payloadDataManager, mock())
            .getChangeAddress(account)
            .test().values().single() `should equal` "CHANGE1"
    }

    @Test
    fun `resolve BCH addresses test`() {
        val bchAccountDecoy = GenericMetadataAccount().apply { xpub = "XPUB1" }
        val bchAccount = GenericMetadataAccount().apply { xpub = "XPUB2" }
        val reference = AccountReference.BitcoinLike(CryptoCurrency.BCH, "", "XPUB2")
        val bchDataManager: BchDataManager = mock {
            on { getActiveAccounts() } `it returns` listOf(bchAccountDecoy, bchAccount)
            on { getNextReceiveCashAddress(1) } `it returns` Observable.just("RECEIVE2")
            on { getNextChangeCashAddress(1) } `it returns` Observable.just("CHANGE2")
        }
        AddressResolver(mock(), mock(), bchDataManager)
            .addressPairForAccount(reference)
            .test().values().single()
            .apply {
                receivingAddress `should equal` "RECEIVE2"
                changeAddress `should equal` "CHANGE2"
            }
        AddressResolver(mock(), mock(), bchDataManager)
            .getChangeAddress(bchAccount)
            .test().values().single() `should equal` "CHANGE2"
    }

    @Test
    fun `resolve Ether test`() {
        val ethereumAccount: EthereumAccount = mock {
            on { checksumAddress } `it returns` "0xETHAddress"
        }
        val reference = AccountReference.Ethereum("", "")
        val accountLookup: AccountLookup = mock {
            on { getAccountFromAddressOrXPub(reference) } `it returns` ethereumAccount
        }
        AddressResolver(accountLookup, mock(), mock())
            .addressPairForAccount(reference)
            .test().values().single()
            .apply {
                receivingAddress `should equal` "0xETHAddress"
                changeAddress `should equal` "0xETHAddress"
            }
    }

    @Test
    fun `resolve XLM test`() {
        AddressResolver(mock(), mock(), mock())
            .addressPairForAccount(AccountReference.Xlm("", "GABCDEFAddress"))
            .test().values().single()
            .apply {
                receivingAddress `should equal` "GABCDEFAddress"
                changeAddress `should equal` "GABCDEFAddress"
            }
    }
}
