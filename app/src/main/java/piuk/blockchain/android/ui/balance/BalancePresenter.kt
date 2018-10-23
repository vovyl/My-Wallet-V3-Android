package piuk.blockchain.android.ui.balance

import android.support.annotation.VisibleForTesting
import com.blockchain.notifications.models.NotificationPayload
import info.blockchain.balance.CryptoCurrency
import info.blockchain.balance.CryptoValue
import info.blockchain.balance.formatWithUnit
import info.blockchain.wallet.api.Environment
import info.blockchain.wallet.ethereum.data.EthAddressResponse
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import piuk.blockchain.android.R
import piuk.blockchain.android.data.datamanagers.TransactionListDataManager
import piuk.blockchain.android.ui.account.ItemAccount
import piuk.blockchain.android.ui.receive.WalletAccountHelper
import piuk.blockchain.android.ui.swipetoreceive.SwipeToReceiveHelper
import piuk.blockchain.android.util.StringUtils
import piuk.blockchain.android.util.extensions.addToCompositeDisposable
import piuk.blockchain.androidbuysell.datamanagers.BuyDataManager
import piuk.blockchain.androidbuysell.datamanagers.CoinifyDataManager
import piuk.blockchain.androidbuysell.models.coinify.BlockchainDetails
import piuk.blockchain.androidbuysell.services.ExchangeService
import piuk.blockchain.androidcore.data.access.AuthEvent
import piuk.blockchain.androidcore.data.api.EnvironmentConfig
import piuk.blockchain.androidcore.data.bitcoincash.BchDataManager
import piuk.blockchain.androidcore.data.currency.CurrencyState
import piuk.blockchain.androidcore.data.ethereum.EthDataManager
import piuk.blockchain.androidcore.data.exchangerate.ExchangeRateDataManager
import piuk.blockchain.androidcore.data.exchangerate.FiatExchangeRates
import piuk.blockchain.androidcore.data.payload.PayloadDataManager
import piuk.blockchain.androidcore.data.rxjava.RxBus
import piuk.blockchain.androidcore.data.shapeshift.ShapeShiftDataManager
import piuk.blockchain.androidcore.utils.PrefsUtil
import piuk.blockchain.androidcore.utils.extensions.applySchedulers
import piuk.blockchain.androidcoreui.ui.base.BasePresenter
import piuk.blockchain.androidcoreui.ui.base.UiState
import timber.log.Timber
import java.math.BigInteger
import javax.inject.Inject

class BalancePresenter @Inject constructor(
    private val exchangeRateDataManager: ExchangeRateDataManager,
    private val transactionListDataManager: TransactionListDataManager,
    private val ethDataManager: EthDataManager,
    private val swipeToReceiveHelper: SwipeToReceiveHelper,
    internal val payloadDataManager: PayloadDataManager,
    private val buyDataManager: BuyDataManager,
    private val stringUtils: StringUtils,
    private val prefsUtil: PrefsUtil,
    private val rxBus: RxBus,
    private val currencyState: CurrencyState,
    private val shapeShiftDataManager: ShapeShiftDataManager,
    private val bchDataManager: BchDataManager,
    private val walletAccountHelper: WalletAccountHelper,
    private val environmentSettings: EnvironmentConfig,
    private val exchangeService: ExchangeService,
    private val coinifyDataManager: CoinifyDataManager,
    private val fiatExchangeRates: FiatExchangeRates
) : BasePresenter<BalanceView>() {

    @VisibleForTesting
    var notificationObservable: Observable<NotificationPayload>? = null
    @VisibleForTesting
    var authEventObservable: Observable<AuthEvent>? = null

    private var shortcutsGenerated = false
    private val tokenSingle: Single<String>
        get() = exchangeService.getExchangeMetaData()
            .cache()
            .addToCompositeDisposable(this)
            .applySchedulers()
            .singleOrError()
            .map { it.coinify!!.token }

    // region Life cycle
    override fun onViewReady() {
        onAccountsAdapterSetup()
        onTxFeedAdapterSetup()
        subscribeToEvents()
        if (environmentSettings.environment == Environment.TESTNET) {
            currencyState.cryptoCurrency = CryptoCurrency.BTC
            view.disableCurrencyHeader()
        }
    }

    override fun onViewDestroyed() {
        notificationObservable?.let { rxBus.unregister(NotificationPayload::class.java, it) }
        authEventObservable?.let { rxBus.unregister(AuthEvent::class.java, it) }
        super.onViewDestroyed()
    }

    private fun subscribeToEvents() {
        authEventObservable = rxBus.register(AuthEvent::class.java).apply {
            subscribe {
                // Clear tx feed
                view.updateTransactionDataSet(
                    currencyState.isDisplayingCryptoCurrency,
                    mutableListOf()
                )
                transactionListDataManager.clearTransactionList()
            }
        }

        notificationObservable = rxBus.register(NotificationPayload::class.java).apply {
            subscribe {
                // no-op
            }
        }
    }
    // endregion

    // region API calls
    private fun refreshAll(account: ItemAccount): Single<Boolean> = getUpdateTickerCompletable()
        .andThen(
            Completable.merge(
                listOf(
                    updateBalancesCompletable(),
                    updateTransactionsListCompletable(account)
                )
            )
        )
        .andThen(getAccounts().map { it.size > 1 })

    internal fun onRefreshRequested() {
        compositeDisposable +=
            getCurrentAccount()
                .flatMap { refreshAll(it) }
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { view.setUiState(UiState.LOADING) }
                .doOnError { view.setUiState(UiState.FAILURE) }
                .doOnSuccess {
                    view.setDropdownVisibility(it)
                    if (!shortcutsGenerated) {
                        shortcutsGenerated = true
                        refreshLauncherShortcuts()
                    }
                    setViewType(currencyState.isDisplayingCryptoCurrency)
                }
                .doOnError { Timber.e(it) }
                .subscribe()
    }

    @VisibleForTesting
    internal fun getUpdateTickerCompletable(): Completable {
        return exchangeRateDataManager.updateTickers()
    }

    @VisibleForTesting
    internal fun updateEthAddress() =
        Completable.fromObservable(ethDataManager.fetchEthAddress()
            .subscribeOn(Schedulers.io())
            .onExceptionResumeNext { Observable.empty<EthAddressResponse>() })

    @VisibleForTesting
    internal fun updateBchWallet() = bchDataManager.refreshMetadataCompletable()
        .subscribeOn(Schedulers.io())
        .doOnError { Timber.e(it) }

    /**
     * API call - Fetches latest balance for selected currency and updates UI balance
     */
    @VisibleForTesting
    internal fun updateBalancesCompletable() =
        when (currencyState.cryptoCurrency) {
            CryptoCurrency.BTC -> payloadDataManager.updateAllBalances()
            CryptoCurrency.ETHER -> ethDataManager.fetchEthAddressCompletable()
            CryptoCurrency.BCH -> bchDataManager.updateAllBalances()
            CryptoCurrency.XLM -> Completable.complete()
        }

    /**
     * API call - Fetches latest transactions for selected currency and account, and updates UI tx list
     */
    @VisibleForTesting
    internal fun updateTransactionsListCompletable(account: ItemAccount): Completable {
        return Completable.fromObservable(
            transactionListDataManager.fetchTransactions(account, 50, 0)
                .doAfterTerminate(this::storeSwipeReceiveAddresses)
                .map { txs ->
                    Observable.zip(
                        getShapeShiftTxNotesObservable(),
                        getCoinifyTxNotesObservable(),
                        mergeReduce()
                    ).addToCompositeDisposable(this)
                        .subscribe(
                            { txNotesMap ->
                                for (tx in txs) {
                                    // Add shapeShift notes
                                    txNotesMap[tx.hash]?.let { tx.note = it }

                                    val cryptoValue = getCryptoValue(currencyState.cryptoCurrency, tx.total)
                                    tx.totalDisplayableCrypto = cryptoValue.formatWithUnit()
                                    tx.totalDisplayableFiat = cryptoValue.getFiatDisplayString()
                                }

                                when {
                                    txs.isEmpty() -> view.setUiState(UiState.EMPTY)
                                    else -> view.setUiState(UiState.CONTENT)
                                }

                                view.updateTransactionDataSet(
                                    currencyState.isDisplayingCryptoCurrency,
                                    txs
                                )
                            },
                            { Timber.e(it) })
                })
    }
    // endregion

    // region Incoming UI events
    /*
    Currency selected from dropdown
     */
    internal fun onCurrencySelected(cryptoCurrency: CryptoCurrency) {
        // Set new currency state
        currencyState.cryptoCurrency = cryptoCurrency

        // Select default account for this currency
        compositeDisposable +=
            getAccounts()
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess {
                    view.setDropdownVisibility(it.size > 1)
                    refreshBalanceHeader(it.first())
                }
                .flatMapCompletable { updateTransactionsListCompletable(it.first()) }
                .doOnSubscribe { view.setUiState(UiState.LOADING) }
                .doOnSubscribe { refreshAccountDataSet() }
                .doOnError { Timber.e(it) }
                .doOnComplete { view.selectDefaultAccount() }
                .subscribeBy(onError = { view.setUiState(UiState.FAILURE) })
    }

    internal fun onGetBitcoinClicked() {
        compositeDisposable +=
            buyDataManager.canBuy
                .subscribe(
                    {
                        if (it && view.shouldShowBuy()) {
                            view.startBuyActivity()
                        } else {
                            view.startReceiveFragmentBtc()
                        }
                    }, { Timber.e(it) }
                )
    }

    /*
    Fetch all active accounts for initial selected currency and set up account adapter
     */
    private fun onAccountsAdapterSetup() {
        compositeDisposable +=
            getAccounts()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy {
                    view.setupAccountsAdapter(it.toMutableList())
                }
    }

    internal fun onAccountSelected(position: Int) {
        compositeDisposable +=
            getAccountAt(position)
                .doOnSubscribe { view.setUiState(UiState.LOADING) }
                .flatMapCompletable {
                    updateTransactionsListCompletable(it)
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnComplete {
                            refreshBalanceHeader(it)
                            refreshAccountDataSet()
                        }
                }
                .doOnError { Timber.e(it) }
                .subscribeBy(onError = { view.setUiState(UiState.FAILURE) })
    }

    /*
    Set fiat or crypto currency state
     */
    internal fun setViewType(showCrypto: Boolean) {
        // Set new currency state
        currencyState.isDisplayingCryptoCurrency = showCrypto

        // Update balance header
        compositeDisposable +=
            getCurrentAccount()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy { refreshBalanceHeader(it) }

        // Update tx list balances
        view.updateTransactionValueType(showCrypto)

        // Update accounts data set
        refreshAccountDataSet()
    }

    /*
    Toggle between fiat - crypto currency
     */
    internal fun onBalanceClick() = setViewType(!currencyState.isDisplayingCryptoCurrency)
    // endregion

    // region Update UI
    internal fun refreshBalanceHeader(account: ItemAccount) {
        view.updateSelectedCurrency(currencyState.cryptoCurrency)
        view.updateBalanceHeader(account.displayBalance ?: "")
    }

    internal fun refreshAccountDataSet() {
        compositeDisposable +=
            getAccounts()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy { view.updateAccountsDataSet(it) }
    }

    private fun refreshLauncherShortcuts() {
        view.generateLauncherShortcuts()
    }
    // endregion

    // region Adapter data
    private fun onTxFeedAdapterSetup() {
        view.setupTxFeedAdapter(currencyState.isDisplayingCryptoCurrency)
    }

    /**
     * Get accounts based on selected currency. Mutable list necessary for adapter. This needs fixing.
     */
    private fun getAccounts() = walletAccountHelper.getAccountItemsForOverview()

    private fun getCurrentAccount(): Single<ItemAccount> = getAccountAt(view.getCurrentAccountPosition() ?: 0)

    private fun getAccountAt(position: Int): Single<ItemAccount> = getAccounts()
        .map { it[if (position < 0 || position >= it.size) 0 else position] }

    private fun getShapeShiftTxNotesObservable() =
        shapeShiftDataManager.getTradesList()
            .addToCompositeDisposable(this)
            .map {
                val mutableMap: MutableMap<String, String> = mutableMapOf()

                for (trade in it) {
                    trade.hashIn?.let {
                        mutableMap.put(it, stringUtils.getString(R.string.morph_deposit_to))
                    }
                    trade.hashOut?.let {
                        mutableMap.put(it, stringUtils.getString(R.string.morph_deposit_from))
                    }
                }
                return@map mutableMap.toMap()
            }
            .doOnError { Timber.e(it) }
            .onErrorReturn { mutableMapOf() }

    private fun getCoinifyTxNotesObservable() =
        tokenSingle.flatMap { coinifyDataManager.getTrades(it).toList() }
            .addToCompositeDisposable(this)
            .map {
                val mutableMap: MutableMap<String, String> = mutableMapOf()
                for (trade in it) {
                    val transfer = if (trade.isSellTransaction()) {
                        trade.transferIn.details as BlockchainDetails
                    } else {
                        trade.transferOut.details as BlockchainDetails
                    }
                    transfer.eventData?.txId?.let {
                        mutableMap.put(
                            it,
                            stringUtils.getFormattedString(
                                R.string.buy_sell_transaction_list_label,
                                trade.id
                            )
                        )
                    }
                }
                return@map mutableMap.toMap()
            }
            .toObservable()
            .doOnError { Timber.e(it) }
            .onErrorReturn { mutableMapOf() }

    private fun storeSwipeReceiveAddresses() {
        // Defer to background thread as deriving addresses is quite processor intensive
        compositeDisposable +=
            Completable.fromCallable {
                swipeToReceiveHelper.updateAndStoreBitcoinAddresses()
                swipeToReceiveHelper.updateAndStoreBitcoinCashAddresses()
                Void.TYPE
            }.subscribeOn(Schedulers.computation())
                .subscribe(
                    { /* No-op */ },
                    { Timber.e(it) })
    }
    // endregion

    // region Helper methods
    private fun getCryptoValue(cryptoCurrency: CryptoCurrency, balance: BigInteger): CryptoValue =
        when (cryptoCurrency) {
            CryptoCurrency.BTC -> CryptoValue.bitcoinFromSatoshis(balance)
            CryptoCurrency.ETHER -> CryptoValue.etherFromWei(balance)
            CryptoCurrency.BCH -> CryptoValue.bitcoinCashFromSatoshis(balance)
            CryptoCurrency.XLM -> CryptoValue.lumensFromStroop(balance)
            else -> throw IllegalArgumentException("${cryptoCurrency.unit} is not currently supported")
        }

    private fun CryptoValue.getFiatDisplayString(): String =
        fiatExchangeRates.getFiat(this).toStringWithSymbol()

    private fun <T, R> mergeReduce(): BiFunction<Map<T, R>, Map<T, R>, Map<T, R>> =
        BiFunction { firstMap, secondMap ->
            mutableMapOf<T, R>().apply {
                putAll(firstMap)
                putAll(secondMap)
            }.toMap()
        }

    internal fun areLauncherShortcutsEnabled() =
        prefsUtil.getValue(PrefsUtil.KEY_RECEIVE_SHORTCUTS_ENABLED, true)

    internal fun getCurrentCurrency() = currencyState.cryptoCurrency
    // endregion
}