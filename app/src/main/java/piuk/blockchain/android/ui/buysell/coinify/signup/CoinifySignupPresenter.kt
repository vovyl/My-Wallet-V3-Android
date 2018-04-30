package piuk.blockchain.android.ui.buysell.coinify.signup

import com.google.common.base.Optional
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import piuk.blockchain.android.util.extensions.addToCompositeDisposable
import piuk.blockchain.androidbuysell.datamanagers.CoinifyDataManager
import piuk.blockchain.androidbuysell.models.CoinifyData
import piuk.blockchain.androidbuysell.models.coinify.KycResponse
import piuk.blockchain.androidbuysell.models.coinify.ReviewState
import piuk.blockchain.androidbuysell.models.coinify.Trader
import piuk.blockchain.androidbuysell.models.coinify.TraderResponse
import piuk.blockchain.androidbuysell.models.coinify.exceptions.CoinifyApiException
import piuk.blockchain.androidbuysell.services.ExchangeService
import piuk.blockchain.androidcore.data.currency.CurrencyState
import piuk.blockchain.androidcore.data.metadata.MetadataManager
import piuk.blockchain.androidcore.data.payload.PayloadDataManager
import piuk.blockchain.androidcore.data.walletoptions.WalletOptionsDataManager
import piuk.blockchain.androidcore.utils.extensions.applySchedulers
import piuk.blockchain.androidcore.utils.extensions.toSerialisedString
import piuk.blockchain.androidcoreui.ui.base.BasePresenter
import timber.log.Timber
import javax.inject.Inject

class CoinifySignupPresenter @Inject constructor(
        private val exchangeService: ExchangeService,
        private val coinifyDataManager: CoinifyDataManager,
        private val payloadDataManager: PayloadDataManager,
        private val currencyState: CurrencyState,
        private val walletOptionsDataManager: WalletOptionsDataManager,
        private val metadataManager: MetadataManager
) : BasePresenter<CoinifySignupView>() {

    private var countryCode: String? = null

    override fun onViewReady() {

        getTraderAccountObservable()
                .applySchedulers()
                .addToCompositeDisposable(this)
                .subscribe {
                    if (it.isPresent) {

                        // Trader account found - Check KYC reviews
                        Timber.d("Coinify trader: " + it.get())

                        getKycReviewListObservable()
                                .applySchedulers()
                                .addToCompositeDisposable(this)
                                .subscribe {
                                    if (it.isPresent) {
                                        // Found KYC reviews
                                        Timber.d("Coinify KYC review list size: " + it.get().size)

                                        // No kyc reviews???

                                        // Multiple  KYC reviews might exist
                                        val completedKycListSize = it.get().filter {
                                            it.state == ReviewState.Completed || it.state == ReviewState.Reviewing}
                                                .toList()
                                                .size

                                        val documentRequested = it.get().filter {
                                            it.state == ReviewState.DocumentsRequested}
                                                .lastOrNull()

                                        if (completedKycListSize > 0) {
                                            // Any Completed or in Review state can continue
                                            view.onStartOverview()
                                        } else if (documentRequested != null){
                                            // DocumentsRequested state will continue from redirect url
                                            view.onStartVerifyIdentification(documentRequested.redirectUrl)
                                        } else {
                                            // Rejected, Failed, Expired state will need to sign up again
                                            view.onStartWelcome()
                                        }

                                    } else {
                                        // No KYC reviews
                                        Timber.d("Coinify No KYC reviews")
                                    }
                                }

                    } else {
                        // No stored metadata for buy sell - Assume no Coinify account
                        view.onStartWelcome()
                    }
                }
    }

    internal fun setCountryCode(selectedCountryCode: String) {
        countryCode = selectedCountryCode
    }

    internal fun signUp(verifiedEmailAddress: String): Completable {

        countryCode?.run {
            return walletOptionsDataManager.getCoinifyPartnerId()
                    .flatMap {
                        coinifyDataManager.getEmailTokenAndSignUp(
                                payloadDataManager.guid,
                                payloadDataManager.sharedKey,
                                verifiedEmailAddress,
                                currencyState.fiatUnit,
                                this,
                                it)
                                .toObservable()
                                .applySchedulers()
                    }
                    .flatMap { saveCoinifyMetadata(it).toObservable() }
                    .flatMap {
                        coinifyDataManager.startKycReview(it.offlineToken).toObservable()
                    }
                    .flatMapCompletable {
                        view.onStartVerifyIdentification(it.redirectUrl)
                        Completable.complete()
                    }
                    .applySchedulers()
                    .doOnError {
                        Timber.e(it)

                        if (it is CoinifyApiException)
                            view.showToast(it.getErrorDescription())

                        view.onFinish()
                    }
        } ?: return Completable.error(Throwable("Country code not set"))
    }

    private fun saveCoinifyMetadata(traderResponse: TraderResponse): Single<TraderResponse> =
        exchangeService.getExchangeMetaData()
                .doOnNext {
                    it.coinify = CoinifyData(
                            traderResponse.trader.id,
                            traderResponse.offlineToken
                    )
                }
                .flatMapCompletable {
                    metadataManager.saveToMetadata(
                            it.toSerialisedString(),
                            ExchangeService.METADATA_TYPE_EXCHANGE
                    )
                }
                .toSingle { traderResponse }

    fun startVerifyIdentification() {
        view.showToast("iSignThis Coming soon!")
        view.onFinish()
    }

    private fun getCoinifyMetaDataObservable(): Observable<Optional<CoinifyData>> =
        exchangeService.getExchangeMetaData()
                .applySchedulers()
                .addToCompositeDisposable(this)
                .map {
                    it.coinify?.run {
                        Optional.of(this)
                    } ?: Optional.absent()
                }

    private fun getTraderAccountObservable(): Observable<Optional<Trader>> {

        return getCoinifyMetaDataObservable()
                .applySchedulers()
                .addToCompositeDisposable(this)
                .flatMap { maybeCoinifyData ->
                    if (maybeCoinifyData.isPresent) {

                        val offlineToken = maybeCoinifyData.get().token

                        getTraderObservable(offlineToken)
                    } else {
                        //No coinify token
                        Observable.just(Optional.absent<Trader>())
                    }
                }
    }

    private fun getTraderObservable(offlineToken: String): Observable<Optional<Trader>> =

            coinifyDataManager.getTrader(offlineToken)
                    .toObservable()
                    .applySchedulers()
                    .addToCompositeDisposable(this)
                    .map { Optional.of(it) }

    private fun getKycReviewListObservable(): Observable<Optional<List<KycResponse>>> {

        return getCoinifyMetaDataObservable()
                .applySchedulers()
                .addToCompositeDisposable(this)
                .flatMap { maybeCoinifyData ->
                    if (maybeCoinifyData.isPresent) {

                        val offlineToken = maybeCoinifyData.get().token

                        getKycReviewsObservable(offlineToken)
                    } else {
                        //No coinify token
                        Observable.just(Optional.absent<List<KycResponse>>())
                    }
                }
    }

    private fun getKycReviewsObservable(offlineToken: String): Observable<Optional<List<KycResponse>>> =

            coinifyDataManager.getKycReviews(offlineToken)
                    .toObservable()
                    .applySchedulers()
                    .addToCompositeDisposable(this)
                    .map { Optional.of(it) }
}