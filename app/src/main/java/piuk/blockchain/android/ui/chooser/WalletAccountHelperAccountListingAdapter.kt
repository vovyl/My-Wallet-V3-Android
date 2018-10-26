package piuk.blockchain.android.ui.chooser

import com.blockchain.ui.chooser.AccountChooserItem
import com.blockchain.ui.chooser.AccountListing
import info.blockchain.balance.CryptoCurrency
import info.blockchain.wallet.payload.data.LegacyAddress
import io.reactivex.Single
import piuk.blockchain.android.ui.account.ItemAccount
import piuk.blockchain.android.ui.receive.WalletAccountHelper

class WalletAccountHelperAccountListingAdapter(
    private val walletAccountHelper: WalletAccountHelper
) : AccountListing {

    override fun accountList(cryptoCurrency: CryptoCurrency): Single<List<AccountChooserItem>> {
        val single: Single<List<ItemAccount>> = when (cryptoCurrency) {
            CryptoCurrency.BTC -> Single.just(walletAccountHelper.getHdAccounts())
            CryptoCurrency.BCH -> Single.just(walletAccountHelper.getHdBchAccounts())
            CryptoCurrency.ETHER -> Single.just(walletAccountHelper.getEthAccount())
            CryptoCurrency.XLM -> walletAccountHelper.getXlmAccount()
        }
        return single.map { it.map { account -> mapAccountSummary(account) } }
    }

    override fun importedList(cryptoCurrency: CryptoCurrency): Single<List<AccountChooserItem>> =
        Single.just(
            when (cryptoCurrency) {
                CryptoCurrency.BTC -> walletAccountHelper.getLegacyAddresses()
                CryptoCurrency.BCH -> walletAccountHelper.getLegacyBchAddresses()
                CryptoCurrency.ETHER -> emptyList()
                CryptoCurrency.XLM -> emptyList()
            }.map(this::mapLegacyAddress)
        )

    private fun mapAccountSummary(it: ItemAccount): AccountChooserItem =
        AccountChooserItem.AccountSummary(
            it.label ?: "",
            it.displayBalance ?: "",
            it.accountObject
        )

    private fun mapLegacyAddress(itemAccount: ItemAccount): AccountChooserItem {
        val legacyAddress = itemAccount.accountObject as? LegacyAddress
        return AccountChooserItem.LegacyAddress(
            label = itemAccount.label ?: "",
            address = if (legacyAddress == null) null else itemAccount.address,
            displayBalance = itemAccount.displayBalance ?: "",
            isWatchOnly = legacyAddress?.isWatchOnly ?: true,
            accountObject = itemAccount.accountObject
        )
    }
}
