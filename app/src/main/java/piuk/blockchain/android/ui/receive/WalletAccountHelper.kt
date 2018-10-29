package piuk.blockchain.android.ui.receive

import com.blockchain.sunriver.XlmDataManager
import info.blockchain.balance.CryptoCurrency
import info.blockchain.balance.CryptoValue
import info.blockchain.wallet.coin.GenericMetadataAccount
import info.blockchain.wallet.payload.PayloadManager
import info.blockchain.wallet.payload.data.Account
import info.blockchain.wallet.payload.data.LegacyAddress
import io.reactivex.Single
import io.reactivex.rxkotlin.zipWith
import org.bitcoinj.core.Address
import piuk.blockchain.android.R
import piuk.blockchain.android.ui.account.ItemAccount
import piuk.blockchain.android.util.StringUtils
import piuk.blockchain.androidcore.data.api.EnvironmentConfig
import piuk.blockchain.androidcore.data.bitcoincash.BchDataManager
import piuk.blockchain.androidcore.data.currency.CurrencyState
import piuk.blockchain.androidcore.data.ethereum.EthDataManager
import piuk.blockchain.androidcore.data.exchangerate.FiatExchangeRates
import piuk.blockchain.androidcore.data.exchangerate.toFiat
import java.math.BigInteger
import java.util.Collections

class WalletAccountHelper(
    private val payloadManager: PayloadManager,
    private val stringUtils: StringUtils,
    private val currencyState: CurrencyState,
    private val ethDataManager: EthDataManager,
    private val bchDataManager: BchDataManager,
    private val xlmDataManager: XlmDataManager,
    private val environmentSettings: EnvironmentConfig,
    private val exchangeRates: FiatExchangeRates
) {
    /**
     * Returns a list of [ItemAccount] objects containing both HD accounts and [LegacyAddress]
     * objects, eg from importing accounts.
     *
     * @return Returns a list of [ItemAccount] objects
     */
    @Deprecated("AND-1535 XLM needs singles - this needs to go")
    fun getAccountItems(cryptoCurrency: CryptoCurrency): List<ItemAccount> = when (cryptoCurrency) {
        CryptoCurrency.BTC -> allBtcAccountItems()
        CryptoCurrency.BCH -> allBchAccountItems()
        CryptoCurrency.ETHER -> getEthAccount()
        CryptoCurrency.XLM -> TODO("AND-1535")
    }

    /**
     * Returns a list of [ItemAccount] objects containing both HD accounts and [LegacyAddress]
     * objects, eg from importing accounts.
     *
     * @return Returns a list of [ItemAccount] objects
     */
    fun accountItems(cryptoCurrency: CryptoCurrency): Single<List<ItemAccount>> =
        when (cryptoCurrency) {
            CryptoCurrency.BTC -> Single.just(allBtcAccountItems())
            CryptoCurrency.BCH -> Single.just(allBchAccountItems())
            CryptoCurrency.ETHER -> Single.just(getEthAccount())
            CryptoCurrency.XLM -> getXlmAccount()
        }

    private fun allBtcAccountItems() = getHdAccounts() + getLegacyAddresses()

    private fun allBchAccountItems() = getHdBchAccounts() + getLegacyBchAddresses()

    /**
     * Returns a list of [ItemAccount] objects containing only HD accounts.
     *
     * @return Returns a list of [ItemAccount] objects
     */
    fun getHdAccounts(): List<ItemAccount> {
        val list = payloadManager.payload?.hdWallets?.get(0)?.accounts
            ?: Collections.emptyList<Account>()
        // Skip archived account
        return list.filterNot { it.isArchived }
            .map {
                ItemAccount(
                    it.label,
                    getBtcAccountBalanceString(it),
                    null,
                    getAccountAbsoluteBalance(it),
                    it,
                    it.xpub
                ).apply { type = ItemAccount.TYPE.SINGLE_ACCOUNT }
            }
    }

    /**
     * Returns a list of [ItemAccount] objects containing only HD accounts.
     *
     * @return Returns a list of [ItemAccount] objects
     */
    fun getHdBchAccounts(): List<ItemAccount> = bchDataManager.getActiveAccounts()
        // Skip archived account
        .filterNot { it.isArchived }
        .map {
            ItemAccount(
                it.label,
                getAccountBalanceBch(it),
                null,
                getAccountAbsoluteBalance(it),
                it,
                it.xpub
            ).apply { type = ItemAccount.TYPE.SINGLE_ACCOUNT }
        }

    /**
     * Returns a list of [ItemAccount] objects containing only [LegacyAddress] objects.
     *
     * @return Returns a list of [ItemAccount] objects
     */
    fun getLegacyAddresses(): List<ItemAccount> {
        val list = payloadManager.payload?.legacyAddressList
            ?: Collections.emptyList<LegacyAddress>()
        // Skip archived address
        return list.filterNot { it.tag == LegacyAddress.ARCHIVED_ADDRESS }
            .map {
                // If address has no label, we'll display address
                var labelOrAddress: String? = it.label
                if (labelOrAddress == null || labelOrAddress.trim { it <= ' ' }.isEmpty()) {
                    labelOrAddress = it.address
                }

                // Watch-only tag - we'll ask for xpriv scan when spending from
                var tag: String? = null
                if (it.isWatchOnly) {
                    tag = stringUtils.getString(R.string.watch_only)
                }

                ItemAccount(
                    labelOrAddress,
                    getAddressBalance(it),
                    tag,
                    getAddressAbsoluteBalance(it),
                    it,
                    it.address
                )
            }
    }

    /**
     * Returns a list of [ItemAccount] objects containing only [LegacyAddress] objects which also
     * have a BCH balance.
     *
     * @return Returns a list of [ItemAccount] objects
     */
    fun getLegacyBchAddresses() = payloadManager.payload.legacyAddressList
        // Skip archived address
        .filterNot { it.tag == LegacyAddress.ARCHIVED_ADDRESS }
        .filterNot {
            bchDataManager.getAddressBalance(it.address).compareTo(BigInteger.ZERO) == 0
        }
        .map {
            val cashAddress = Address.fromBase58(
                environmentSettings.bitcoinCashNetworkParameters,
                it.address
            ).toCashAddress().removeBchUri()
            // If address has no label, we'll display address
            var labelOrAddress: String? = it.label
            if (labelOrAddress == null || labelOrAddress.trim { it <= ' ' }.isEmpty()) {
                labelOrAddress = cashAddress
            }

            // Watch-only tag - we'll ask for xpriv scan when spending from
            var tag: String? = null
            if (it.isWatchOnly) {
                tag = stringUtils.getString(R.string.watch_only)
            }

            ItemAccount(
                labelOrAddress,
                getBchAddressBalance(it),
                tag,
                getAddressAbsoluteBalance(it),
                it,
                cashAddress
            )
        }

    /**
     * Returns a list of [ItemAccount] objects containing only [LegacyAddress] objects,
     * specifically from the list of address book entries.
     *
     * @return Returns a list of [ItemAccount] objects
     */
    fun getAddressBookEntries() = payloadManager.payload.addressBook?.map {
        ItemAccount(
            if (it.label.isNullOrEmpty()) it.address else it.label,
            "",
            stringUtils.getString(R.string.address_book_label),
            null,
            null,
            it.address
        )
    } ?: emptyList()

    fun getDefaultOrFirstFundedAccount(): ItemAccount = when (currencyState.cryptoCurrency) {
        CryptoCurrency.BTC -> getDefaultOrFirstFundedBtcAccount()
        CryptoCurrency.BCH -> getDefaultOrFirstFundedBchAccount()
        CryptoCurrency.ETHER -> getDefaultEthAccount()
        CryptoCurrency.XLM -> TODO("AND-1535")
    }

    fun getEthAccount() =
        listOf(getDefaultEthAccount())

    fun getXlmAccount(): Single<List<ItemAccount>> =
        getDefaultXlmAccountItem().map { listOf(it) }

    /**
     * Returns the balance of an [Account] in Satoshis (BTC)
     */
    private fun getAccountAbsoluteBalance(account: Account) =
        payloadManager.getAddressBalance(account.xpub).toLong()

    /**
     * Returns the balance of a [GenericMetadataAccount] in Satoshis (BCH)
     */
    private fun getAccountAbsoluteBalance(account: GenericMetadataAccount) =
        bchDataManager.getAddressBalance(account.xpub).toLong()

    /**
     * Returns the balance of an [Account], formatted for display.
     */
    private fun getBtcAccountBalanceString(account: Account): String {
        return CryptoValue
            .bitcoinFromSatoshis(payloadManager.getAddressBalance(account.xpub))
            .toBalanceString()
    }

    /**
     * Returns the balance of a [GenericMetadataAccount], formatted for display.
     */
    private fun getAccountBalanceBch(account: GenericMetadataAccount): String {
        return CryptoValue
            .bitcoinCashFromSatoshis(getAccountAbsoluteBalance(account))
            .toBalanceString()
    }

    /**
     * Returns the balance of a [LegacyAddress] in Satoshis
     */
    private fun getAddressAbsoluteBalance(legacyAddress: LegacyAddress) =
        payloadManager.getAddressBalance(legacyAddress.address).toLong()

    /**
     * Returns the balance of a [LegacyAddress] in Satoshis
     */
    private fun getBchAddressAbsoluteBalance(legacyAddress: LegacyAddress) =
        bchDataManager.getAddressBalance(legacyAddress.address).toLong()

    /**
     * Returns the balance of a [LegacyAddress], formatted for display
     */
    private fun getAddressBalance(legacyAddress: LegacyAddress): String {
        return CryptoValue
            .bitcoinFromSatoshis(getAddressAbsoluteBalance(legacyAddress))
            .toBalanceString()
    }

    /**
     * Returns the balance of a [LegacyAddress] in BCH, formatted for display
     */
    private fun getBchAddressBalance(legacyAddress: LegacyAddress): String {
        return CryptoValue
            .bitcoinCashFromSatoshis(getBchAddressAbsoluteBalance(legacyAddress))
            .toBalanceString()
    }

    private fun getDefaultOrFirstFundedBtcAccount(): ItemAccount {
        var account =
            payloadManager.payload.hdWallets[0].accounts[payloadManager.payload.hdWallets[0].defaultAccountIdx]

        if (getAccountAbsoluteBalance(account) <= 0L)
            for (funded in payloadManager.payload.hdWallets[0].accounts) {
                if (!funded.isArchived && getAccountAbsoluteBalance(funded) > 0L) {
                    account = funded
                    break
                }
            }

        return ItemAccount(
            account.label,
            getBtcAccountBalanceString(account),
            null,
            getAccountAbsoluteBalance(account),
            account,
            account.xpub
        )
    }

    private fun getDefaultOrFirstFundedBchAccount(): ItemAccount {
        var account = bchDataManager.getDefaultGenericMetadataAccount()!!

        if (getAccountAbsoluteBalance(account) <= 0L)
            for (funded in bchDataManager.getActiveAccounts()) {
                if (getAccountAbsoluteBalance(funded) > 0L) {
                    account = funded
                    break
                }
            }

        return ItemAccount(
            account.label,
            getAccountBalanceBch(account),
            null,
            getAccountAbsoluteBalance(account),
            account,
            account.xpub
        )
    }

    private fun getDefaultEthAccount(): ItemAccount {
        val ethModel = ethDataManager.getEthResponseModel()
        val ethAccount = ethDataManager.getEthWallet()!!.account
        val balance = CryptoValue.etherFromWei(ethModel?.getTotalBalance() ?: BigInteger.ZERO)

        return ItemAccount(
            ethAccount?.label,
            balance.toBalanceString(),
            null,
            0,
            ethAccount,
            ethAccount?.address!!
        )
    }

    private fun getDefaultXlmAccountItem() =
        xlmDataManager.defaultAccount()
            .zipWith(xlmDataManager.getBalance())
            .map { (account, balance) ->
                ItemAccount(
                    account.label,
                    balance.toBalanceString(),
                    null,
                    balance.amount.toLong(),
                    null,
                    account.accountId
                )
            }

    /**
     * Returns a list of [ItemAccount] objects containing both HD accounts and [LegacyAddress]
     * objects, eg from importing accounts.
     */
    fun getAccountItemsForOverview(): Single<List<ItemAccount>> =
        when (currencyState.cryptoCurrency) {
            CryptoCurrency.BTC -> Single.just(getBtcOverviewList())
            CryptoCurrency.BCH -> Single.just(getBchOverviewList())
            CryptoCurrency.ETHER -> Single.just(getEthOverviewList())
            CryptoCurrency.XLM -> getDefaultXlmAccountItem().map { listOf(it) }
        }

    private fun getEthOverviewList(): List<ItemAccount> = getEthAccount()

    private fun getBchOverviewList(): MutableList<ItemAccount> {
        return mutableListOf<ItemAccount>().apply {

            val legacyAddresses = getLegacyBchAddresses()
            val accounts = getHdBchAccounts()

            // Create "All Accounts" if necessary
            if (accounts.size > 1 || legacyAddresses.isNotEmpty()) {
                add(getBchWalletAccountItem())
            }
            addAll(accounts)

            // Create consolidated "Imported Addresses"
            if (!legacyAddresses.isEmpty()) {
                add(getBchImportedAddressesAccountItem())
            }
        }
    }

    private fun getBtcOverviewList(): List<ItemAccount> {
        return mutableListOf<ItemAccount>().apply {

            val legacyAddresses = getLegacyAddresses()
            val accounts = getHdAccounts()

            // Create "All Accounts" if necessary
            if (accounts.size > 1 || legacyAddresses.isNotEmpty()) {
                add(getBtcWalletAccountItem())
            }
            addAll(accounts)

            // Create consolidated "Imported Addresses"
            if (!legacyAddresses.isEmpty()) {
                add(getBtcImportedAddressesAccountItem())
            }
        }.toList()
    }

    private fun getBtcWalletAccountItem(): ItemAccount {
        val bigIntBalance = payloadManager.walletBalance

        return ItemAccount().apply {
            label = stringUtils.getString(R.string.all_accounts)
            absoluteBalance = bigIntBalance.toLong()
            displayBalance = getBalanceString(
                currencyState.isDisplayingCryptoCurrency,
                CryptoValue(
                    CryptoCurrency.BTC, bigIntBalance
                )
            )
            type = ItemAccount.TYPE.ALL_ACCOUNTS_AND_LEGACY
        }
    }

    private fun getBchWalletAccountItem(): ItemAccount {
        val bigIntBalance = bchDataManager.getWalletBalance()

        return ItemAccount().apply {
            label = stringUtils.getString(R.string.bch_all_accounts)
            absoluteBalance = bigIntBalance.toLong()
            displayBalance = getBalanceString(
                currencyState.isDisplayingCryptoCurrency,
                CryptoValue(
                    CryptoCurrency.BCH, bigIntBalance
                )
            )
            type = ItemAccount.TYPE.ALL_ACCOUNTS_AND_LEGACY
        }
    }

    private fun getBtcImportedAddressesAccountItem(): ItemAccount {
        val bigIntBalance = payloadManager.importedAddressesBalance

        return ItemAccount().apply {
            label = stringUtils.getString(R.string.imported_addresses)
            absoluteBalance = bigIntBalance.toLong()
            displayBalance = getBalanceString(
                currencyState.isDisplayingCryptoCurrency,
                CryptoValue(CryptoCurrency.BTC, bigIntBalance)
            )
            type = ItemAccount.TYPE.ALL_LEGACY
        }
    }

    private fun getBchImportedAddressesAccountItem(): ItemAccount {
        val bigIntBalance = bchDataManager.getImportedAddressBalance()

        return ItemAccount().apply {
            label = stringUtils.getString(R.string.bch_imported_addresses)
            absoluteBalance = bigIntBalance.toLong()
            displayBalance = getBalanceString(
                currencyState.isDisplayingCryptoCurrency,
                CryptoValue(CryptoCurrency.BCH, bigIntBalance)
            )
            type = ItemAccount.TYPE.ALL_LEGACY
        }
    }

    @Deprecated("Use Display mode overload")
    private fun getBalanceString(showCrypto: Boolean, balance: CryptoValue): String {
        val money = if (showCrypto) balance else balance.toFiat(exchangeRates)
        return money.toStringWithSymbol()
    }

    private fun CryptoValue.toBalanceString() =
        when (currencyState.displayMode) {
            CurrencyState.DisplayMode.Crypto -> this
            CurrencyState.DisplayMode.Fiat -> this.toFiat(exchangeRates)
        }.toStringWithSymbol()

    // /////////////////////////////////////////////////////////////////////////
    // Extension functions
    // /////////////////////////////////////////////////////////////////////////

    private fun String.removeBchUri(): String = this.replace("bitcoincash:", "")

    fun hasMultipleEntries(cryptoCurrency: CryptoCurrency) =
        when (cryptoCurrency) {
            CryptoCurrency.BTC -> allBtcAccountItems().size + getAddressBookEntries().size
            CryptoCurrency.ETHER -> getEthAccount().size
            CryptoCurrency.BCH -> allBchAccountItems().size
            CryptoCurrency.XLM -> 1 // TODO("AND-1511") Ideally we're getting real account count here, even if one
        } > 1
}
