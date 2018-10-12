package piuk.blockchain.android.ui.dashboard

import android.support.annotation.DrawableRes
import android.support.annotation.VisibleForTesting
import com.blockchain.balance.canTint
import com.blockchain.balance.drawableRes
import com.blockchain.kyc.models.nabu.KycState
import com.blockchain.kyc.models.nabu.UserState
import com.blockchain.kycui.settings.KycStatusHelper
import com.blockchain.lockbox.data.LockboxDataManager
import info.blockchain.balance.CryptoCurrency
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.rxkotlin.zipWith
import io.reactivex.schedulers.Schedulers
import piuk.blockchain.android.R
import piuk.blockchain.android.ui.balance.AnnouncementData
import piuk.blockchain.android.ui.balance.ImageLeftAnnouncementCard
import piuk.blockchain.android.ui.balance.ImageRightAnnouncementCard
import piuk.blockchain.android.ui.dashboard.models.OnboardingModel
import piuk.blockchain.android.ui.home.MainActivity
import piuk.blockchain.android.ui.home.models.MetadataEvent
import piuk.blockchain.android.ui.onboarding.OnboardingPagerContent
import piuk.blockchain.android.ui.swipetoreceive.SwipeToReceiveHelper
import piuk.blockchain.android.util.StringUtils
import piuk.blockchain.android.util.extensions.addToCompositeDisposable
import piuk.blockchain.androidbuysell.datamanagers.BuyDataManager
import piuk.blockchain.androidcore.data.access.AccessState
import piuk.blockchain.androidcore.data.bitcoincash.BchDataManager
import piuk.blockchain.androidcore.data.currency.CurrencyFormatManager
import piuk.blockchain.androidcore.data.exchangerate.ExchangeRateDataManager
import piuk.blockchain.androidcore.data.rxjava.RxBus
import piuk.blockchain.androidcore.utils.PrefsUtil
import piuk.blockchain.androidcore.utils.extensions.applySchedulers
import piuk.blockchain.androidcore.utils.helperfunctions.unsafeLazy
import piuk.blockchain.androidcoreui.ui.base.BasePresenter
import piuk.blockchain.androidcoreui.utils.logging.BalanceLoadedEvent
import piuk.blockchain.androidcoreui.utils.logging.Logging
import timber.log.Timber
import java.text.DecimalFormat

class DashboardPresenter(
    private val dashboardBalanceCalculator: DashboardData,
    private val prefsUtil: PrefsUtil,
    private val exchangeRateFactory: ExchangeRateDataManager,
    private val bchDataManager: BchDataManager,
    private val stringUtils: StringUtils,
    private val accessState: AccessState,
    private val buyDataManager: BuyDataManager,
    private val rxBus: RxBus,
    private val swipeToReceiveHelper: SwipeToReceiveHelper,
    private val currencyFormatManager: CurrencyFormatManager,
    private val kycStatusHelper: KycStatusHelper,
    private val lockboxDataManager: LockboxDataManager
) : BasePresenter<DashboardView>() {

    private val currencies = DashboardConfig.currencies

    private val displayList by unsafeLazy {
        (listOf(
            stringUtils.getString(R.string.dashboard_balances),
            PieChartsState.Loading,
            stringUtils.getString(R.string.dashboard_price_charts)
        ) + currencies.map {
            AssetPriceCardState.Loading(it)
        }).toMutableList()
    }

    private val metadataObservable by unsafeLazy {
        rxBus.register(
            MetadataEvent::class.java
        )
    }

    override fun onViewReady() {
        with(view) {
            notifyItemAdded(displayList, 0)
            scrollToTop()
        }
        updatePrices()

        val observable = when (firstRun) {
            true -> metadataObservable
            false -> Observable.just(MetadataEvent.SETUP_COMPLETE)
                .applySchedulers()
                // If data is present, update with cached data
                // Data updates run anyway but this makes the UI nicer to look at whilst loading
                .doOnNext {
                    cachedData?.run { view.updatePieChartState(this) }
                }
        }

        firstRun = false

        // Triggers various updates to the page once all metadata is loaded
        observable.flatMap { getOnboardingStatusObservable() }
            // Clears subscription after single event
            .firstOrError()
            .doOnSuccess { updateAllBalances() }
            .doOnSuccess { checkLatestAnnouncements() }
            .doOnSuccess { swipeToReceiveHelper.storeEthAddress() }
            .addToCompositeDisposable(this)
            .subscribe(
                { /* No-op */ },
                { Timber.e(it) }
            )
    }

    fun updateBalances() {

        with(view) {
            scrollToTop()
        }

        updatePrices()
        updateAllBalances()
    }

    override fun onViewDestroyed() {
        rxBus.unregister(MetadataEvent::class.java, metadataObservable)
        super.onViewDestroyed()
    }

    private fun updatePrices() {
        exchangeRateFactory.updateTickers()
            .addToCompositeDisposable(this)
            .doOnError { Timber.e(it) }
            .subscribe(
                {
                    handleAssetPriceUpdate(
                        currencies.map {
                            AssetPriceCardState.Data(
                                getPriceString(it),
                                it,
                                it.drawableRes()
                            )
                        })
                },
                {
                    handleAssetPriceUpdate(
                        currencies.map {
                            AssetPriceCardState.Error(it)
                        }
                    )
                }
            )
    }

    private fun handleAssetPriceUpdate(list: List<AssetPriceCardState>) {
        displayList.removeAll { it is AssetPriceCardState }
        displayList.addAll(list)

        val firstPosition = displayList.indexOfFirst { it is AssetPriceCardState }

        val positions = (firstPosition until firstPosition + list.size).toList()

        view.notifyItemUpdated(displayList, positions)
    }

    private fun updateAllBalances() {
        dashboardBalanceCalculator.getPieChartData()
            .zipWith(lockboxDataManager.hasLockbox())
            .map {
                it.first.copy(hasLockbox = it.second)
            }
            .observeOn(AndroidSchedulers.mainThread())
            .addToCompositeDisposable(this)
            .subscribe(
                {
                    Logging.logCustom(
                        BalanceLoadedEvent(
                            hasBtcBalance = !it.bitcoin.spendable.isZero,
                            hasBchBalance = !it.bitcoinCash.spendable.isZero,
                            hasEthBalance = !it.ether.spendable.isZero,
                            hasXlmBalance = !it.lumen.spendable.isZero
                        )
                    )
                    cachedData = it
                    view.updatePieChartState(it)
                    storeSwipeToReceiveAddresses()
                },
                { Timber.e(it) }
            )
    }

    private fun showAnnouncement(index: Int, announcementData: AnnouncementData) {
        displayList.add(index, announcementData)
        with(view) {
            notifyItemAdded(displayList, index)
            scrollToTop()
        }
    }

    private fun dismissAnnouncement(prefKey: String) {
        displayList.filterIsInstance<AnnouncementData>()
            .forEachIndexed { index, any ->
                if (any.prefsKey == prefKey) {
                    displayList.remove(any)
                    with(view) {
                        notifyItemRemoved(displayList, index)
                        scrollToTop()
                    }
                }
            }
    }

    private fun getOnboardingStatusObservable(): Observable<Boolean> = if (isOnboardingComplete()) {
        Observable.just(false)
    } else {
        buyDataManager.canBuy
            .addToCompositeDisposable(this)
            .doOnNext { displayList.removeAll { it is OnboardingModel } }
            .doOnNext { displayList.add(0, getOnboardingPages(it)) }
            .doOnNext { view.notifyItemAdded(displayList, 0) }
            .doOnNext { view.scrollToTop() }
            .doOnError { Timber.e(it) }
    }

    private fun checkLatestAnnouncements() {
        // If user hasn't completed onboarding, ignore announcements
        if (isOnboardingComplete()) {
            displayList.removeAll { it is AnnouncementData }
            checkNativeBuySellAnnouncement()
            checkKycPrompt()
        }
    }

    private fun checkNativeBuySellAnnouncement() {
        val buyPrefKey = NATIVE_BUY_SELL_DISMISSED
        buyDataManager.isCoinifyAllowed
            .addToCompositeDisposable(this)
            .subscribeBy(
                onNext = {
                    if (it && !prefsUtil.getValue(buyPrefKey, false)) {
                        prefsUtil.setValue(buyPrefKey, true)

                        val announcementData = ImageLeftAnnouncementCard(
                            title = R.string.announcement_trading_cta,
                            description = R.string.announcement_trading_description,
                            link = R.string.announcement_trading_link,
                            image = R.drawable.vector_buy_onboarding,
                            emoji = null,
                            closeFunction = { dismissAnnouncement(buyPrefKey) },
                            linkFunction = { view.startBuyActivity() },
                            prefsKey = buyPrefKey
                        )
                        showAnnouncement(0, announcementData)
                    }
                },
                onError = { Timber.e(it) }
            )
    }

    private fun checkKycPrompt() {
        if (!prefsUtil.getValue(KYC_INCOMPLETE_DISMISSED, false)) {
            compositeDisposable +=
                getKycStatus()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeBy(
                        onSuccess = { (userState, kycStatus) ->
                            if ((userState == UserState.Created || userState == UserState.Active) &&
                                kycStatus == KycState.None
                            ) {
                                val kycIncompleteData = ImageRightAnnouncementCard(
                                    title = R.string.buy_sell_verify_your_identity,
                                    description = R.string.kyc_drop_off_card_description,
                                    link = R.string.kyc_drop_off_card_button,
                                    image = R.drawable.vector_kyc_onboarding,
                                    closeFunction = {
                                        prefsUtil.setValue(KYC_INCOMPLETE_DISMISSED, true)
                                        dismissAnnouncement(KYC_INCOMPLETE_DISMISSED)
                                    },
                                    linkFunction = { view.startKycFlow() },
                                    prefsKey = KYC_INCOMPLETE_DISMISSED
                                )
                                showAnnouncement(0, kycIncompleteData)
                            }
                        },
                        onError = { Timber.e(it) }
                    )
        }
    }

    private fun getKycStatus(): Single<Pair<UserState, KycState>> = Single.zip(
        kycStatusHelper.getUserState(),
        kycStatusHelper.getKycStatus(),
        BiFunction { userState: UserState, kycStatus: KycState -> userState to kycStatus }
    ).onErrorReturn { UserState.None to KycState.None }

    private fun getOnboardingPages(isBuyAllowed: Boolean): OnboardingModel {
        val pages = mutableListOf<OnboardingPagerContent>()

        if (isBuyAllowed) {
            // Buy bitcoin prompt
            pages.add(
                OnboardingPagerContent(
                    stringUtils.getString(R.string.onboarding_current_price),
                    getFormattedPriceString(CryptoCurrency.BTC),
                    stringUtils.getString(R.string.onboarding_buy_content),
                    stringUtils.getString(R.string.onboarding_buy_bitcoin),
                    MainActivity.ACTION_BUY,
                    R.color.primary_blue_accent,
                    R.drawable.vector_buy_offset
                )
            )
        }
        // Receive bitcoin
        pages.add(
            OnboardingPagerContent(
                stringUtils.getString(R.string.onboarding_receive_bitcoin),
                "",
                stringUtils.getString(R.string.onboarding_receive_content),
                stringUtils.getString(R.string.receive_bitcoin),
                MainActivity.ACTION_RECEIVE,
                R.color.secondary_teal_medium,
                R.drawable.vector_receive_offset
            )
        )
        // QR Codes
        pages.add(
            OnboardingPagerContent(
                stringUtils.getString(R.string.onboarding_qr_codes),
                "",
                stringUtils.getString(R.string.onboarding_qr_codes_content),
                stringUtils.getString(R.string.onboarding_scan_address),
                MainActivity.ACTION_SEND,
                R.color.primary_navy_medium,
                R.drawable.vector_qr_offset
            )
        )

        return OnboardingModel(
            pages,
            // TODO: These are neat and clever, but make things pretty hard to test. Replace with callbacks.
            dismissOnboarding = {
                setOnboardingComplete(true)
                displayList.removeAll { it is OnboardingModel }
                view.notifyItemRemoved(displayList, 0)
                view.scrollToTop()
            },
            onboardingComplete = { setOnboardingComplete(true) },
            onboardingNotComplete = { setOnboardingComplete(false) }
        )
    }

    private fun isOnboardingComplete() =
    // If wallet isn't newly created, don't show onboarding
        prefsUtil.getValue(
            PrefsUtil.KEY_ONBOARDING_COMPLETE,
            false
        ) || !accessState.isNewlyCreated

    private fun setOnboardingComplete(completed: Boolean) {
        prefsUtil.setValue(PrefsUtil.KEY_ONBOARDING_COMPLETE, completed)
    }

    private fun storeSwipeToReceiveAddresses() {
        bchDataManager.getWalletTransactions(50, 0)
            .flatMapCompletable { getSwipeToReceiveCompletable() }
            .addToCompositeDisposable(this)
            .subscribe(
                { view.startWebsocketService() },
                { Timber.e(it) }
            )
    }

    private fun getSwipeToReceiveCompletable(): Completable =
    // Defer to background thread as deriving addresses is quite processor intensive
        Completable.fromCallable {
            swipeToReceiveHelper.updateAndStoreBitcoinAddresses()
            swipeToReceiveHelper.updateAndStoreBitcoinCashAddresses()
        }.subscribeOn(Schedulers.computation())
            // Ignore failure
            .onErrorComplete()

    // /////////////////////////////////////////////////////////////////////////
    // Units
    // /////////////////////////////////////////////////////////////////////////

    private fun getFormattedPriceString(cryptoCurrency: CryptoCurrency): String {
        val lastPrice = getLastPrice(cryptoCurrency, getFiatCurrency())
        val fiatSymbol = currencyFormatManager.getFiatSymbol(getFiatCurrency(), view.locale)
        val format = DecimalFormat().apply { minimumFractionDigits = 2 }

        return stringUtils.getFormattedString(
            R.string.current_price_btc,
            "$fiatSymbol${format.format(lastPrice)}"
        )
    }

    private fun getPriceString(cryptoCurrency: CryptoCurrency): String =
        getLastPrice(cryptoCurrency, getFiatCurrency()).run { getFormattedCurrencyString(this) }

    private fun getFormattedCurrencyString(price: Double): String {
        return currencyFormatManager.getFormattedFiatValueWithSymbol(price)
    }

    private fun getFiatCurrency() =
        prefsUtil.getValue(PrefsUtil.KEY_SELECTED_FIAT, PrefsUtil.DEFAULT_CURRENCY)

    private fun getLastPrice(cryptoCurrency: CryptoCurrency, fiat: String) =
        exchangeRateFactory.getLastPrice(cryptoCurrency, fiat)

    companion object {

        @VisibleForTesting
        internal const val KYC_INCOMPLETE_DISMISSED = "KYC_INCOMPLETE_DISMISSED"

        @VisibleForTesting
        internal const val NATIVE_BUY_SELL_DISMISSED = "NATIVE_BUY_SELL_DISMISSED"

        /**
         * This field stores whether or not the presenter has been run for the first time across
         * all instances. This allows the page to load without a metadata set-up event, which won't
         * be present if the the page is being returned to.
         */
        @VisibleForTesting
        var firstRun = true

        /**
         * This is intended to be a temporary solution to caching data on this page. In future,
         * I intend to organise the MainActivity fragment backstack so that the DashboardFragment
         * is never killed intentionally. However, this could introduce a lot of bugs so this will
         * do for now.
         */
        private var cachedData: PieChartsState.Data? = null

        @JvmStatic
        fun onLogout() {
            firstRun = true
            cachedData = null
        }
    }
}

sealed class AssetPriceCardState(val currency: CryptoCurrency) {

    data class Data(
        val priceString: String,
        val cryptoCurrency: CryptoCurrency,
        @DrawableRes val icon: Int
    ) : AssetPriceCardState(cryptoCurrency) {
        val allowTint = canTint(icon)
    }

    class Loading(val cryptoCurrency: CryptoCurrency) : AssetPriceCardState(cryptoCurrency)
    class Error(val cryptoCurrency: CryptoCurrency) : AssetPriceCardState(cryptoCurrency)
}