package com.blockchain.datamanagers

import com.nhaarman.mockito_kotlin.mock
import info.blockchain.balance.AccountReference
import info.blockchain.balance.CryptoCurrency
import info.blockchain.wallet.coin.GenericMetadataAccount
import info.blockchain.wallet.ethereum.EthereumAccount
import info.blockchain.wallet.ethereum.EthereumWallet
import info.blockchain.wallet.payload.data.Account
import org.amshove.kluent.`it returns`
import org.amshove.kluent.`should be`
import org.amshove.kluent.`should throw the Exception`
import org.amshove.kluent.`with message`
import org.junit.Test
import piuk.blockchain.androidcore.data.bitcoincash.BchDataManager
import piuk.blockchain.androidcore.data.ethereum.EthDataManager
import piuk.blockchain.androidcore.data.payload.PayloadDataManager

class AccountLookupTest {

    @Test
    fun `can lookup BTC account`() {
        val account = Account()
        val payloadDataManager = mock<PayloadDataManager> {
            on { getAccountForXPub("XPUB1") } `it returns` account
        }
        AccountLookup(payloadDataManager, mock(), mock())
            .getAccountFromAddressOrXPub(
                AccountReference.BitcoinLike(CryptoCurrency.BTC, "", "XPUB1")
            ) `should be` account
    }

    @Test
    fun `can lookup BCH account`() {
        val bchAccountDecoy = GenericMetadataAccount().apply { xpub = "XPUB1" }
        val bchAccount = GenericMetadataAccount().apply { xpub = "XPUB2" }
        val bchDataManager: BchDataManager = mock {
            on { getActiveAccounts() } `it returns` listOf(bchAccountDecoy, bchAccount)
        }
        AccountLookup(mock(), bchDataManager, mock())
            .getAccountFromAddressOrXPub(
                AccountReference.BitcoinLike(CryptoCurrency.BCH, "", "XPUB2")
            ) `should be` bchAccount
    }

    @Test
    fun `can lookup ETHER account`() {
        val ethereumAccount: EthereumAccount = mock()
        val mock: EthereumWallet = mock {
            on { account } `it returns` ethereumAccount
        }
        val ethDataManager: EthDataManager = mock {
            on { getEthWallet() } `it returns` mock
        }
        AccountLookup(mock(), mock(), ethDataManager)
            .getAccountFromAddressOrXPub(
                AccountReference.Ethereum("", "")
            ) `should be` ethereumAccount
    }

    @Test
    fun `can't lookup XLM account`() {
        {
            AccountLookup(mock(), mock(), mock())
                .getAccountFromAddressOrXPub(
                    AccountReference.Xlm("", "")
                )
        } `should throw the Exception` IllegalArgumentException::class `with message`
            "Access to the XLM Json data is not allowed or required"
    }
}
