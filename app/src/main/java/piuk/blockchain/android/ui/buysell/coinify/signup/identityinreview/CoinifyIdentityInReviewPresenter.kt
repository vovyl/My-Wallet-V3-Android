package piuk.blockchain.android.ui.buysell.coinify.signup.identityinreview

import android.support.annotation.VisibleForTesting
import com.google.common.base.Optional
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import piuk.blockchain.android.util.extensions.addToCompositeDisposable
import piuk.blockchain.androidbuysell.datamanagers.CoinifyDataManager
import piuk.blockchain.androidbuysell.models.CoinifyData
import piuk.blockchain.androidbuysell.models.coinify.KycResponse
import piuk.blockchain.androidbuysell.models.coinify.ReviewState
import piuk.blockchain.androidbuysell.services.ExchangeService
import piuk.blockchain.androidcore.utils.extensions.applySchedulers
import piuk.blockchain.androidcoreui.ui.base.BasePresenter
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class CoinifyIdentityInReviewPresenter @Inject constructor(
        private val exchangeService: ExchangeService,
        private val coinifyDataManager: CoinifyDataManager
) : BasePresenter<CoinifyIdentityInReviewView>() {

    override fun onViewReady() {

        Observable.timer(2, TimeUnit.SECONDS, AndroidSchedulers.mainThread())
                .doOnSubscribe { view.onShowLoading() }
                .flatMap { getCoinifyMetaDataObservable() }
                .applySchedulers()
                .addToCompositeDisposable(this)
                .flatMapCompletable { optionalCoinifyData ->
                    if (optionalCoinifyData.isPresent) {
                        // User has coinify account - Continue signup or go to overview
                        continueTraderSignupOrGoToOverviewCompletable(optionalCoinifyData.get())
                    } else {
                        // This will never happen but handle case anyway
                        view.onFinish()
                        Completable.complete()
                    }
                }
                .subscribe(
                        { /* No-op */ },
                        {
                            Timber.e(it)
                            view.onFinish()
                        }
                )
    }

    private fun continueTraderSignupOrGoToOverviewCompletable(coinifyData: CoinifyData) =
            coinifyDataManager.getTrader(coinifyData.token!!)
                    .flatMap {
                        // Trader exists - Check for any KYC reviews
                        coinifyDataManager.getKycReviews(coinifyData.token!!)
                    }.flatMapCompletable { kycList ->

                        filterReviewStatus(kycList)
                        Completable.complete()
                    }

    @VisibleForTesting
    fun filterReviewStatus(kycList: List<KycResponse>) {

        if (kycList.any { it.state == ReviewState.Completed }) {
            // Unlikely to see this result - after supplying docs status will be pending
            // otherwise we will go straight to overview
            view.onShowCompleted()
        } else if (kycList.any { it.state == ReviewState.Reviewing }) {
            // Unlikely to see this result - after supplying docs status will be pending
            // otherwise we will go straight to overview
            view.onShowReviewing()
        } else if (kycList.any { it.state == ReviewState.Pending }) {
            // Please supply proof
            // Very likely that the back button was pressed
            view.onShowPending()
        } else if (kycList.any { it.state == ReviewState.DocumentsRequested }) {
            // Unlikely to see this result
            view.onShowDocumentsRequested()
        } else if (kycList.any { it.state == ReviewState.Expired }) {
            // Unlikely to see this result - User would be redirected to supply docs again before getting to this fragment
            view.onShowExpired()
        } else if (kycList.any { it.state == ReviewState.Failed }) {
            // We get stuck with the below cases
            // Can't create new account with same email
            // redirectUrl isn't valid - Same issue on web
            view.onShowFailed()
        } else if (kycList.any { it.state == ReviewState.Rejected }) {
            view.onShowRejected()
        } else {
            view.onFinish()
        }
    }

    /**
     * Fetches coinify data from metadata store i.e offline token and user/trader id.
     *
     * @return An [Observable] wrapping an [Optional] with coinify data
     */
    private fun getCoinifyMetaDataObservable() =
            exchangeService.getExchangeMetaData()
                    .applySchedulers()
                    .addToCompositeDisposable(this)
                    .map {
                        it.coinify?.run {
                            Optional.of(this)
                        } ?: Optional.absent()
                    }
}