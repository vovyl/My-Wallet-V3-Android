package piuk.blockchain.androidcore.data.bitcoincash

import info.blockchain.balance.AccountReference
import info.blockchain.wallet.coin.GenericMetadataAccount
import io.reactivex.Single

/**
 * Generates a bech32 Bitcoin Cash receive address for an account at a given position. The
 * address returned will be the next unused in the chain.
 *
 * @param account The [GenericMetadataAccount] you wish to generate an address from
 * @return A Bitcoin Cash receive address in bech32 format
 */
fun BchDataManager.nextReceiveCashAddress(account: GenericMetadataAccount): Single<String> =
    getNextReceiveCashAddress(index(account)).singleOrError()

/**
 * Generates a bech32 Bitcoin Cash receive address for an account at a given position. The
 * address returned will be the next unused in the chain.
 *
 * @param account The [AccountReference] you wish to generate an address from
 * @return A Bitcoin Cash receive address in bech32 format
 */
fun BchDataManager.nextReceiveCashAddress(account: AccountReference.BitcoinLike): Single<String> =
    getNextReceiveCashAddress(index(account)).singleOrError()

/**
 * Generates a bech32 Bitcoin Cash change address for an account at a given position. The
 * address returned will be the next unused in the chain.
 *
 * @param account The [GenericMetadataAccount] you wish to generate an address from
 * @return A Bitcoin Cash change address in bech32 format
 */
fun BchDataManager.nextChangeCashAddress(account: GenericMetadataAccount): Single<String> =
    getNextChangeCashAddress(index(account)).singleOrError()

/**
 * Generates a bech32 Bitcoin Cash change address for an account at a given position. The
 * address returned will be the next unused in the chain.
 *
 * @param account The [AccountReference] you wish to generate an address from
 * @return A Bitcoin Cash change address in bech32 format
 */
fun BchDataManager.nextChangeCashAddress(account: AccountReference.BitcoinLike): Single<String> =
    getNextChangeCashAddress(index(account)).singleOrError()

private fun BchDataManager.index(account: GenericMetadataAccount): Int = index(account.xpub)

private fun BchDataManager.index(account: AccountReference.BitcoinLike): Int = index(account.xpub)

private fun BchDataManager.index(xpub: String): Int =
    getActiveAccounts().indexOfFirst { it.xpub == xpub }
