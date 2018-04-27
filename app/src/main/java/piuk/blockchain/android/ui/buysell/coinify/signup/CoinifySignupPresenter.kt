package piuk.blockchain.android.ui.buysell.coinify.signup

import com.google.common.base.Optional
import io.reactivex.Observable
import piuk.blockchain.android.util.extensions.addToCompositeDisposable
import piuk.blockchain.androidbuysell.datamanagers.CoinifyDataManager
import piuk.blockchain.androidbuysell.models.coinify.KycResponse
import piuk.blockchain.androidbuysell.models.coinify.ReviewState
import piuk.blockchain.androidbuysell.models.coinify.TraderResponse
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

        startKycIdObservable()
                .applySchedulers()
                .addToCompositeDisposable(this)
                .doOnError {
                    Timber.e(it)
                    view.showToast((it as CoinifyApiException).getErrorDescription())
                    view.onStartWelcome()
                }
                .subscribe { kycResponse ->

                    if (kycResponse.isPresent) {
                        val kyc = kycResponse.get()
                        Timber.d("vos kyc: "+kyc.state)
                        when (kyc.state) {
                            ReviewState.Rejected -> view.onStartWelcome()
                            ReviewState.Failed -> view.onStartWelcome()
                            ReviewState.Expired -> view.onStartWelcome()

                            ReviewState.Completed -> view.onStartOverview()
                            ReviewState.Reviewing -> view.onStartOverview()

                            ReviewState.DocumentsRequested, ReviewState.Pending ->
                                view.onStartVerifyIdentification(kyc.redirectUrl)
                        }
                    } else {
                        // No previous sign up
                        view.onStartWelcome()
                    }
                }

    }

    internal fun setCountryCode(selectedCountryCode: String) {
        countryCode = selectedCountryCode
    }

    internal fun signUp(verifiedEmailAddress: String): Observable<TraderResponse> {

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
                    .applySchedulers()
                    .doOnError {
                        Timber.e(it)
                        view.showToast((it as CoinifyApiException).getErrorDescription())
                        view.onStartWelcome()
                    }
        } ?: return Observable.error(Throwable("Country code not set"))
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

        //TODO might want to use getKycReviewStatus here

//        exchangeService.getExchangeMetaData()
//                .applySchedulers()
//                .addToCompositeDisposable(this)
//                .subscribe {
//                    it?.coinify?.token?.run {
//                        coinifyDataManager.startKycReview(this)
//                                .map {
//                                    view.onStartVerifyIdentification(it.redirectUrl)
//                                }
//                                .toObservable()
//                                .applySchedulers()
//
//                    } ?: view.onStartWelcome()
//                }
    }
}