package piuk.blockchain.android.ui.buysell.coinify.signup

import com.google.common.base.Optional
import io.reactivex.Observable
import piuk.blockchain.android.util.extensions.addToCompositeDisposable
import piuk.blockchain.androidbuysell.datamanagers.CoinifyDataManager
import piuk.blockchain.androidbuysell.models.coinify.KycResponse
import piuk.blockchain.androidbuysell.models.coinify.ReviewState
import piuk.blockchain.androidbuysell.models.coinify.TraderResponse
import piuk.blockchain.androidbuysell.models.coinify.exceptions.CoinifyApiException
import piuk.blockchain.androidbuysell.models.coinify.exceptions.CoinifyErrorCodes
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

        getKycIdObservable()
                .applySchedulers()
                .addToCompositeDisposable(this)
                .doOnError {
                    // This shouldn't happen, but handle case anyway
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

                            // TODO Pass return url?
                            ReviewState.DocumentsRequested, ReviewState.Pending ->
                                view.onStartVerifyIdentification()
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

    private fun getKycIdObservable(): Observable<Optional<KycResponse>> {
        return exchangeService.getExchangeMetaData()
                .applySchedulers()
                .flatMap {
                    it?.coinify?.token?.run {
                        coinifyDataManager.startKycReview(this)
                                .map {
                                    Optional.of(it)
                                }
                                .toObservable()
                                .applySchedulers()

                    } ?: Observable.just(Optional.absent())
                }
    }
}