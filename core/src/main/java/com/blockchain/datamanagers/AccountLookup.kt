package com.blockchain.datamanagers

import com.blockchain.serialization.JsonSerializableAccount
import info.blockchain.balance.AccountReference
import info.blockchain.balance.CryptoCurrency
import piuk.blockchain.androidcore.data.bitcoincash.BchDataManager
import piuk.blockchain.androidcore.data.ethereum.EthDataManager
import piuk.blockchain.androidcore.data.payload.PayloadDataManager

internal class AccountLookup(
    private val payloadDataManager: PayloadDataManager,
    private val bchDataManager: BchDataManager,
    private val ethDataManager: EthDataManager
) {

    fun getAccountFromAddressOrXPub(accountReference: AccountReference): JsonSerializableAccount =
        when (accountReference.cryptoCurrency) {
            CryptoCurrency.BTC -> payloadDataManager.getAccountForXPub(
                (accountReference as AccountReference.BitcoinLike).xpub
            )
            CryptoCurrency.BCH -> {
                val xpub = (accountReference as AccountReference.BitcoinLike).xpub
                bchDataManager.getActiveAccounts()
                    .asSequence()
                    .filter { it.xpub == xpub }
                    .first()
            }
            CryptoCurrency.ETHER -> ethDataManager.getEthWallet()!!.account
            CryptoCurrency.XLM -> throw IllegalArgumentException(
                "Access to the XLM Json data is not allowed or required"
            )
        }
}
