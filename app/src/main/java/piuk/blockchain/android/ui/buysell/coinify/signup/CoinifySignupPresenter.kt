package piuk.blockchain.android.ui.buysell.coinify.signup

import com.google.common.base.Optional
import io.reactivex.Completable
import io.reactivex.Observable
import piuk.blockchain.android.util.extensions.addToCompositeDisposable
import piuk.blockchain.androidbuysell.datamanagers.CoinifyDataManager
import piuk.blockchain.androidbuysell.models.CoinifyData
import piuk.blockchain.androidbuysell.models.coinify.ReviewState
import piuk.blockchain.androidbuysell.services.ExchangeService
import piuk.blockchain.androidcore.utils.extensions.applySchedulers
import piuk.blockchain.androidcoreui.ui.base.BasePresenter
import timber.log.Timber
import javax.inject.Inject

class CoinifySignupPresenter @Inject constructor(
        private val exchangeService: ExchangeService,
        private val coinifyDataManager: CoinifyDataManager
) : BasePresenter<CoinifySignupView>() {

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
                .subscribe(
                        {
                            // No-op
                        },
                        {
                            Timber.e(it)
                            // TODO
                            view.showToast("${it.message}")
                            view.onFinish()
                        }
                )
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
                    }
                    .flatMapCompletable { kycList ->
                        if (kycList.isEmpty()) {
                            // Kyc review not started yet
                            startKycReviewProcess(coinifyData)

                        } else {
                            // Multiple  KYC reviews might exist
                            val completedKycListSize = kycList.count {
                                it.state == ReviewState.Completed || it.state == ReviewState.Reviewing
                            }

                            val pendingState = kycList.lastOrNull {
                                it.state == ReviewState.DocumentsRequested || it.state == ReviewState.Pending
                            }

                            when {
                            // Any Completed or in Review state can continue
                                completedKycListSize > 0 -> view.onStartOverview()
                            // DocumentsRequested state will continue from redirect url
                                pendingState != null -> view.onStartVerifyIdentification(
                                        pendingState.redirectUrl
                                )
                            // Rejected, Failed, Expired state will need to KYC again
                                else -> return@flatMapCompletable startKycReviewProcess(coinifyData)
                            }

                            Completable.complete()
                        }
                    }

    internal fun continueVerifyIdentification() {
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

    private fun startKycReviewProcess(coinifyData: CoinifyData): Completable =
            coinifyDataManager.startKycReview(coinifyData.token!!)
                    .flatMapCompletable {
                        view.onStartVerifyIdentification(it.redirectUrl)
                        Completable.complete()
                    }
                    .applySchedulers()
}