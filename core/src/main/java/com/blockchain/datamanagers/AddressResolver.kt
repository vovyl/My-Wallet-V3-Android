package com.blockchain.datamanagers

import info.blockchain.balance.AccountReference
import info.blockchain.balance.CryptoCurrency
import info.blockchain.wallet.coin.GenericMetadataAccount
import info.blockchain.wallet.ethereum.EthereumAccount
import info.blockchain.wallet.payload.data.Account
import io.reactivex.Single
import io.reactivex.rxkotlin.zipWith
import piuk.blockchain.androidcore.data.bitcoincash.BchDataManager
import piuk.blockchain.androidcore.data.bitcoincash.nextChangeCashAddress
import piuk.blockchain.androidcore.data.bitcoincash.nextReceiveCashAddress
import piuk.blockchain.androidcore.data.payload.PayloadDataManager

internal class AddressResolver(
    private val accountLookup: AccountLookup,
    private val payloadDataManager: PayloadDataManager,
    private val bchDataManager: BchDataManager
) {

    fun addressPairForAccount(
        reference: AccountReference
    ): Single<AddressPair> =
        when (reference.cryptoCurrency) {
            CryptoCurrency.BTC -> {
                val accountForXPub = accountLookup.getAccountFromAddressOrXPub(reference) as Account
                getReceiveAddress(accountForXPub)
                    .zipWith(getChangeAddress(accountForXPub))
                    .map { (from, change) ->
                        AddressPair(from, change)
                    }
            }
            CryptoCurrency.BCH -> {
                val account = reference as AccountReference.BitcoinLike
                getReceiveAddress(account)
                    .zipWith(getChangeAddress(account))
                    .map { (from, change) ->
                        AddressPair(from, change)
                    }
            }
            CryptoCurrency.ETHER -> {
                val account = accountLookup.getAccountFromAddressOrXPub(reference) as EthereumAccount
                val address = account.checksumAddress
                Single.just(AddressPair(address, address))
            }
            CryptoCurrency.XLM -> {
                val receivingAddress = (reference as AccountReference.Xlm).accountId
                Single.just(AddressPair(receivingAddress, receivingAddress))
            }
        }

    private fun getReceiveAddress(account: Account): Single<String> =
        payloadDataManager.getNextReceiveAddress(account).singleOrError()

    private fun getReceiveAddress(account: AccountReference.BitcoinLike): Single<String> =
        bchDataManager.nextReceiveCashAddress(account)

    internal fun getChangeAddress(account: Account): Single<String> =
        payloadDataManager.getNextChangeAddress(account).singleOrError()

    internal fun getChangeAddress(account: GenericMetadataAccount): Single<String> =
        bchDataManager.nextChangeCashAddress(account)

    private fun getChangeAddress(account: AccountReference.BitcoinLike): Single<String> =
        bchDataManager.nextChangeCashAddress(account)
}

class AddressPair(val receivingAddress: String, val changeAddress: String)
