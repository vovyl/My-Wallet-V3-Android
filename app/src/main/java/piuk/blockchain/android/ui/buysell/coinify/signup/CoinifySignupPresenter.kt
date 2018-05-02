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

        getCoinifyMetaDataObservable()
                .applySchedulers()
                .addToCompositeDisposable(this)
                .flatMapCompletable { optionalCoinifyData ->
                    if (optionalCoinifyData.isPresent) {
                        // User has coinify account - Continue signup or go to overview
                        continueTraderSignupOrGoToOverviewCompletable(optionalCoinifyData.get())
                    } else {
                        // No stored metadata for buy sell - Assume no Coinify account
                        view.onStartWelcome()
                        Completable.complete()
                    }
                }
                .subscribe({
                    // No-op
                }, {
                    Timber.e(it)
                    // TODO
                    view.showToast("${it.message}")
                    view.onFinish()
                })
    }

    /**
     * Calculates the user/trader's signup status and redirects to proper fragment.
     *
     * Passed KYC state = Go to overview
     * DocumentsRequested, Pending = Go to redirect URL
     * Rejected, Failed, Expired = Allow user to reattempt sign up
     *
     * @return [Completable]
     */
    private fun continueTraderSignupOrGoToOverviewCompletable(coinifyData: CoinifyData) =
            coinifyDataManager.getTrader(coinifyData.token!!)
                    .flatMap {
                        // Trader exists - Check for any KYC reviews
                        coinifyDataManager.getKycReviews(coinifyData.token!!)
                    }.flatMapCompletable { kycList ->

                        if (kycList.isEmpty()) {
                            // Kyc review not started yet
                            coinifyDataManager.startKycReview(coinifyData.token!!)
                                    .flatMapCompletable {
                                        view.onStartVerifyIdentification(it.redirectUrl)
                                        Completable.complete()
                                    }
                                    .applySchedulers()

                        } else {

                            // Multiple  KYC reviews might exist
                            val completedKycListSize = kycList.filter {
                                it.state == ReviewState.Completed || it.state == ReviewState.Reviewing
                            }.toList().size

                            val pendingState = kycList.lastOrNull {
                                it.state == ReviewState.DocumentsRequested || it.state == ReviewState.Pending
                            }

                            if (completedKycListSize > 0) {
                                // Any Completed or in Review state can continue
                                view.onStartOverview()
                            } else if (pendingState != null) {
                                // DocumentsRequested state will continue from redirect url
                                view.onStartVerifyIdentification(pendingState.redirectUrl)
                            } else {
                                // Rejected, Failed, Expired state will need to sign up again
                                view.onStartWelcome()
                            }

                            Completable.complete()
                        }
                    }

    internal fun setCountryCode(selectedCountryCode: String) {
        countryCode = selectedCountryCode
    }

    /**
     * Creates Coinify account.
     * Saves Coinify metadata.
     * Starts KYC review process and navigates to iSignThis webview.
     *
     * @return [Completable]
     */
    fun signUp(verifiedEmailAddress: String): Observable<KycResponse> {

        if (countryCode == null) {
            return Observable.error(Throwable("Country code not set"))
        } else {
            return walletOptionsDataManager.getCoinifyPartnerId()
                    .flatMap {
                        coinifyDataManager.getEmailTokenAndSignUp(
                                payloadDataManager.guid,
                                payloadDataManager.sharedKey,
                                verifiedEmailAddress,
                                currencyState.fiatUnit,
                                countryCode!!,
                                it
                        )
                                .toObservable()
                                .applySchedulers()
                    }
                    .flatMap {
                        saveCoinifyMetadata(it).toObservable()
                    }
                    .flatMap {
                        coinifyDataManager.startKycReview(it.offlineToken)
                                .toObservable()
                                .applySchedulers()
                    }
                    .doOnError {
                        Timber.e(it)

                        if (it is CoinifyApiException)
                            view.showToast(it.getErrorDescription())

                        view.onFinish()
                    }
        }
    }

    /**
     * Saves user/trader's offline token and user id.
     *
     * @param traderResponse to be saved in metadata store
     * @return [Single] wrapping a [TraderResponse] object
     */
    private fun saveCoinifyMetadata(traderResponse: TraderResponse): Single<TraderResponse> =
            exchangeService.getExchangeMetaData()
                    .applySchedulers()
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

    fun continueVerifyIdentification() {
        onViewReady()
    }

    /**
     * Fetches coinify data from metadata store i.e offline token and user/trader id.
     *
     * @return An [Observable] wrapping an [Optional] with coinify data
     */
    private fun getCoinifyMetaDataObservable(): Observable<Optional<CoinifyData>> =
            exchangeService.getExchangeMetaData()
                    .applySchedulers()
                    .addToCompositeDisposable(this)
                    .map {
                        it.coinify?.run {
                            Optional.of(this)
                        } ?: Optional.absent()
                    }
}