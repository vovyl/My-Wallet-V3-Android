package piuk.blockchain.android.ui.swipetoreceive

import com.blockchain.sunriver.XlmDataManager
import com.blockchain.sunriver.toUri
import info.blockchain.api.data.Balance
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import org.bitcoinj.core.Address
import piuk.blockchain.android.R
import piuk.blockchain.android.util.StringUtils
import piuk.blockchain.androidcore.data.api.EnvironmentConfig
import piuk.blockchain.androidcore.data.bitcoincash.BchDataManager
import piuk.blockchain.androidcore.data.ethereum.EthDataManager
import piuk.blockchain.androidcore.data.payload.PayloadDataManager
import piuk.blockchain.androidcore.utils.PrefsUtil
import piuk.blockchain.androidcore.utils.extensions.applySchedulers
import timber.log.Timber
import java.math.BigInteger

class SwipeToReceiveHelper(
    private val payloadDataManager: PayloadDataManager,
    private val prefsUtil: PrefsUtil,
    private val ethDataManager: EthDataManager,
    private val bchDataManager: BchDataManager,
    private val stringUtils: StringUtils,
    private val environmentSettings: EnvironmentConfig,
    private val xlmDataManager: XlmDataManager
) {

    fun storeAll(): Completable = Completable.merge(
        listOf(
            updateAndStoreBitcoinAddresses(),
            updateAndStoreBitcoinCashAddresses(),
            storeEthAddress(),
            storeXlmAddress()
        )
    )

    private fun storeBitcoinAddresses() {
        if (getIfSwipeEnabled()) {
            val numOfAddresses = 5

            val defaultAccount = payloadDataManager.defaultAccount
            val receiveAccountName = defaultAccount.label
            store(KEY_SWIPE_RECEIVE_BTC_ACCOUNT_NAME, receiveAccountName)

            val stringBuilder = StringBuilder()

            for (i in 0 until numOfAddresses) {
                val receiveAddress =
                    payloadDataManager.getReceiveAddressAtPosition(defaultAccount, i)
                    // Likely not initialized yet
                        ?: break

                stringBuilder.append(receiveAddress)
                    .append(",")
            }

            store(KEY_SWIPE_RECEIVE_BTC_ADDRESSES, stringBuilder.toString())
        }
    }

    /**
     * Derives 5 addresses from the current point on the receive chain. Stores them alongside the
     * account name in SharedPrefs. Only stores addresses if enabled in SharedPrefs. This should be
     * called on a Computation thread as it can take up to 2 seconds on a mid-range device.
     */
    fun updateAndStoreBitcoinAddresses(): Completable =
        Completable.fromCallable { storeBitcoinAddresses() }

    private fun storeBitcoinCashAddresses() {
        if (getIfSwipeEnabled()) {
            val numOfAddresses = 5

            val defaultAccount = bchDataManager.getDefaultGenericMetadataAccount()!!
            val defaultAccountPosition = bchDataManager.getDefaultAccountPosition()
            val receiveAccountName = defaultAccount.label
            store(KEY_SWIPE_RECEIVE_BCH_ACCOUNT_NAME, receiveAccountName)

            val stringBuilder = StringBuilder()

            for (i in 0 until numOfAddresses) {
                val receiveAddress =
                    bchDataManager.getReceiveAddressAtPosition(defaultAccountPosition, i)
                    // Likely not initialized yet
                        ?: break

                stringBuilder.append(receiveAddress).append(",")
            }

            store(KEY_SWIPE_RECEIVE_BCH_ADDRESSES, stringBuilder.toString())
        }
    }

    /**
     * Derives 5 addresses from the current point on the receive chain. Stores them alongside the
     * account name in SharedPrefs. Only stores addresses if enabled in SharedPrefs. This should be
     * called on a Computation thread as it can take up to 2 seconds on a mid-range device.
     */
    fun updateAndStoreBitcoinCashAddresses(): Completable =
        Completable.fromCallable { storeBitcoinCashAddresses() }

    /**
     * Stores the user's ETH address locally in SharedPrefs. Only stores addresses if enabled in
     * SharedPrefs.
     */
    fun storeEthAddress(): Completable = if (getIfSwipeEnabled()) {
        Maybe.fromCallable { ethDataManager.getEthWallet()?.account?.address }
            .doOnSuccess {
                it?.let { store(KEY_SWIPE_RECEIVE_ETH_ADDRESS, it) }
                    ?: Timber.e("ETH Wallet was null when attempting to store ETH address")
            }
            .ignoreElement()
    } else {
        Completable.complete()
    }

    fun storeXlmAddress(): Completable = if (getIfSwipeEnabled()) {
        xlmDataManager.defaultAccount()
            .doOnSuccess { store(KEY_SWIPE_RECEIVE_XLM_ADDRESS, it.toUri()) }
            .doOnError { Timber.e("Error fetching XLM account when attempting to store XLM address") }
            .toCompletable()
            .onErrorComplete()
    } else {
        Completable.complete()
    }

    /**
     * Returns the next unused Bitcoin address from the list of 5 stored for swipe to receive. Can
     * return an empty String if no unused addresses are found.
     */
    fun getNextAvailableBitcoinAddressSingle(): Single<String> {
        return getBalanceOfAddresses(getBitcoinReceiveAddresses())
            .map { map ->
                for ((address, value) in map) {
                    val balance = value.finalBalance
                    if (balance.compareTo(BigInteger.ZERO) == 0) {
                        return@map address
                    }
                }
                return@map ""
            }.singleOrError()
    }

    /**
     * Returns the next unused Bitcoin Cash address from the list of 5 stored for swipe to receive.
     * Can return an empty String if no unused addresses are found.
     */
    fun getNextAvailableBitcoinCashAddressSingle(): Single<String> {
        return getBalanceOfBchAddresses(getBitcoinCashReceiveAddresses())
            .map { map ->
                for ((address, value) in map) {
                    val balance = value.finalBalance
                    if (balance.compareTo(BigInteger.ZERO) == 0) {
                        return@map Address.fromBase58(
                            environmentSettings.bitcoinCashNetworkParameters,
                            address
                        ).toCashAddress()
                    }
                }
                return@map ""
            }.singleOrError()
    }

    /**
     * Returns a List of the next 5 available unused (at the time of storage) receive addresses. Can
     * return an empty list.
     */
    fun getBitcoinReceiveAddresses(): List<String> {
        val addressString = prefsUtil.getValue(KEY_SWIPE_RECEIVE_BTC_ADDRESSES, "")
        return when {
            addressString.isEmpty() -> emptyList()
            else -> addressString.split(",").dropLastWhile { it.isEmpty() }
        }
    }

    /**
     * Returns a List of the next 5 available unused (at the time of storage) receive addresses for
     * Bitcoin Cash. Can return an empty list.
     */
    fun getBitcoinCashReceiveAddresses(): List<String> {
        val addressString = prefsUtil.getValue(KEY_SWIPE_RECEIVE_BCH_ADDRESSES, "")
        return when {
            addressString.isEmpty() -> emptyList()
            else -> addressString.split(",").dropLastWhile { it.isEmpty() }
        }
    }

    /**
     * Returns the previously stored Ethereum address, wrapped in a [Single].
     */
    fun getEthReceiveAddressSingle(): Single<String> = Single.just(getEthReceiveAddress())

    /**
     * Returns the previously stored Ethereum address or null if not stored
     */
    fun getEthReceiveAddress(): String? =
        prefsUtil.getValue(KEY_SWIPE_RECEIVE_ETH_ADDRESS, null)

    fun getXlmReceiveAddressSingle(): Single<String> = Single.just(getXlmReceiveAddress())

    fun getXlmReceiveAddress(): String? =
        prefsUtil.getValue(KEY_SWIPE_RECEIVE_XLM_ADDRESS, null)

    /**
     * Returns the Bitcoin account name associated with the receive addresses.
     */
    fun getBitcoinAccountName(): String = prefsUtil.getValue(KEY_SWIPE_RECEIVE_BTC_ACCOUNT_NAME, "")

    /**
     * Returns the Bitcoin Cash account name associated with the receive addresses.
     */
    fun getBitcoinCashAccountName(): String = prefsUtil.getValue(
        KEY_SWIPE_RECEIVE_BCH_ACCOUNT_NAME,
        stringUtils.getString(R.string.bch_default_account_label)
    )

    /**
     * Returns the default account name for new Ethereum accounts.
     */
    fun getEthAccountName(): String = stringUtils.getString(R.string.eth_default_account_label)

    fun getXlmAccountName(): String = stringUtils.getString(R.string.xlm_default_account_label)

    private fun getIfSwipeEnabled(): Boolean =
        prefsUtil.getValue(PrefsUtil.KEY_SWIPE_TO_RECEIVE_ENABLED, true)

    private fun getBalanceOfAddresses(addresses: List<String>): Observable<LinkedHashMap<String, Balance>> =
        payloadDataManager.getBalanceOfAddresses(addresses)
            .applySchedulers()

    private fun getBalanceOfBchAddresses(addresses: List<String>): Observable<LinkedHashMap<String, Balance>> =
        payloadDataManager.getBalanceOfBchAddresses(addresses)
            .applySchedulers()

    private fun store(key: String, data: String) {
        prefsUtil.setValue(key, data)
    }

    fun clearStoredData() {
        prefsUtil.removeValue(KEY_SWIPE_RECEIVE_BTC_ADDRESSES)
        prefsUtil.removeValue(KEY_SWIPE_RECEIVE_ETH_ADDRESS)
        prefsUtil.removeValue(KEY_SWIPE_RECEIVE_BCH_ADDRESSES)
        prefsUtil.removeValue(KEY_SWIPE_RECEIVE_XLM_ADDRESS)
        prefsUtil.removeValue(KEY_SWIPE_RECEIVE_BTC_ACCOUNT_NAME)
        prefsUtil.removeValue(KEY_SWIPE_RECEIVE_BCH_ACCOUNT_NAME)
    }

    internal companion object {
        const val KEY_SWIPE_RECEIVE_BTC_ADDRESSES = "swipe_receive_addresses"
        const val KEY_SWIPE_RECEIVE_ETH_ADDRESS = "swipe_receive_eth_address"
        const val KEY_SWIPE_RECEIVE_BCH_ADDRESSES = "swipe_receive_bch_addresses"
        const val KEY_SWIPE_RECEIVE_XLM_ADDRESS = "key_swipe_receive_xlm_address"
        const val KEY_SWIPE_RECEIVE_BTC_ACCOUNT_NAME = "swipe_receive_account_name"
        const val KEY_SWIPE_RECEIVE_BCH_ACCOUNT_NAME = "swipe_receive_bch_account_name"
    }
}
