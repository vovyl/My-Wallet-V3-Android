package piuk.blockchain.android.ui.buysell.coinify.signup

import com.google.common.base.Optional
import io.reactivex.Completable
import io.reactivex.Observable
import piuk.blockchain.android.util.extensions.addToCompositeDisposable
import piuk.blockchain.androidbuysell.datamanagers.CoinifyDataManager
import piuk.blockchain.androidbuysell.models.CoinifyData
import piuk.blockchain.androidbuysell.models.coinify.KycResponse
import piuk.blockchain.androidbuysell.models.coinify.exceptions.CoinifyApiException
import piuk.blockchain.androidbuysell.services.ExchangeService
import piuk.blockchain.androidcore.data.currency.CurrencyState
import piuk.blockchain.androidcore.data.payload.PayloadDataManager
import piuk.blockchain.androidcore.data.walletoptions.WalletOptionsDataManager
import piuk.blockchain.androidcore.utils.extensions.applySchedulers
import piuk.blockchain.androidcoreui.ui.base.BasePresenter
import timber.log.Timber
import javax.inject.Inject

class CoinifySignupPresenter @Inject constructor(
        private val exchangeService: ExchangeService,
        private val coinifyDataManager: CoinifyDataManager,
        private val payloadDataManager: PayloadDataManager,
        private val currencyState: CurrencyState,
        private val walletOptionsDataManager: WalletOptionsDataManager
) : BasePresenter<CoinifySignupView>() {

    private var countryCode: String? = null

    override fun onViewReady() {

        // TODO For dogfood build purpose
        view.onStartWelcome()

//        getCurrentKycReviewStatusObservable()
//                .applySchedulers()
//                .addToCompositeDisposable(this)
//                .doOnError {
//                    Timber.e(it)
//                    view.onStartWelcome()
//                }
//                .subscribe { kycResponse ->
//
//                    if (kycResponse.isPresent) {
//                        val kyc = kycResponse.get()
//                        Timber.d("meh Kyc state: "+kyc.state)
//                        when (kyc.state) {
//                            ReviewState.Rejected -> view.onStartWelcome()
//                            ReviewState.Failed -> view.onStartWelcome()
//                            ReviewState.Expired -> view.onStartWelcome()
//
//                            ReviewState.Completed -> view.onStartOverview()
//                            ReviewState.Reviewing -> view.onStartOverview()
//
//                            ReviewState.DocumentsRequested, ReviewState.Pending ->
//                                view.onStartVerifyIdentification(kyc.redirectUrl)
//                        }
//                    } else {
//                        Timber.d("No Kyc reponse from review id - start Coinify sign up.")
//                        view.onStartWelcome()
//                    }
//                }
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
                    .flatMap { startKycIdObservable() }
                    .flatMapCompletable {
                        // TODO
                        Completable.complete()
                    }
                    .applySchedulers()
                    .doOnError {
                        Timber.e(it)
                        // TODO
                        if (it is CoinifyApiException)
                            view.showToast(it.getErrorDescription())

                        view.onStartWelcome()
                    }
        } ?: return Completable.error(Throwable("Country code not set"))
    }

    private fun startKycIdObservable(): Observable<Optional<KycResponse>> {
        return exchangeService.getExchangeMetaData()
                .applySchedulers()
                .addToCompositeDisposable(this)
                .flatMap {
                    it.coinify?.token?.run {
                        coinifyDataManager.startKycReview(this)
                                .map {
                                    Optional.of(it)
                                }
                                .toObservable()
                                .applySchedulers()

                    } ?: Observable.just(Optional.absent())
                }
    }

    fun startVerifyIdentification() {
        view.showToast("iSignThis Coming soon!")
        view.onFinish()
    }

    private fun getCurrentKycReviewStatusObservable(): Observable<Optional<KycResponse>> {

        return getCoinifyMetaDataObservable()
                .applySchedulers()
                .addToCompositeDisposable(this)
                .flatMap { maybeCoinifyData ->
                    if (maybeCoinifyData.isPresent) {

                        val offlineToken = maybeCoinifyData.get().token
                        val traderId = maybeCoinifyData.get().user

                        // TODO This is so broken atm
                        getKycReviewStatusObservable(offlineToken, traderId)
                    } else {
                        //No coinify token
                        Observable.just(Optional.absent<KycResponse>())
                    }
                }
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

    private fun getKycReviewStatusObservable(offlineToken: String, kycReviewId: Int): Observable<Optional<KycResponse>> =

        coinifyDataManager.getKycReviewStatus(offlineToken, kycReviewId)
                .toObservable()
                .applySchedulers()
                .addToCompositeDisposable(this)
                .map { Optional.of(it) }
}