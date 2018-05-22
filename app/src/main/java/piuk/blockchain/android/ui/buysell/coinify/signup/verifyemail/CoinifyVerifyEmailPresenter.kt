package piuk.blockchain.android.ui.buysell.coinify.signup.verifyemail

import android.support.annotation.VisibleForTesting
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import piuk.blockchain.android.util.extensions.addToCompositeDisposable
import piuk.blockchain.androidbuysell.datamanagers.CoinifyDataManager
import piuk.blockchain.androidbuysell.models.CoinifyData
import piuk.blockchain.androidbuysell.models.coinify.KycResponse
import piuk.blockchain.androidbuysell.models.coinify.TraderResponse
import piuk.blockchain.androidbuysell.services.ExchangeService
import piuk.blockchain.androidcore.data.currency.CurrencyState
import piuk.blockchain.androidcore.data.metadata.MetadataManager
import piuk.blockchain.androidcore.data.payload.PayloadDataManager
import piuk.blockchain.androidcore.data.settings.SettingsDataManager
import piuk.blockchain.androidcore.data.walletoptions.WalletOptionsDataManager
import piuk.blockchain.androidcore.utils.extensions.applySchedulers
import piuk.blockchain.androidcore.utils.extensions.toSerialisedString
import piuk.blockchain.androidcoreui.ui.base.BasePresenter
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class CoinifyVerifyEmailPresenter @Inject constructor(
        private val settingsDataManager: SettingsDataManager,
        private val walletOptionsDataManager: WalletOptionsDataManager,
        private val payloadDataManager: PayloadDataManager,
        private val exchangeService: ExchangeService,
        private val coinifyDataManager: CoinifyDataManager,
        private val metadataManager: MetadataManager,
        private val currencyState: CurrencyState
        ) : BasePresenter<CoinifyVerifyEmailView>() {

    private var verifiedEmailAddress: String? = null

    override fun onViewReady() {

        settingsDataManager.fetchSettings()
                .applySchedulers()
                .addToCompositeDisposable(this)
                .subscribe ({ settings ->

                    view.onEnableContinueButton(settings.isEmailVerified)

                    if (settings.isEmailVerified) {
                        setVerifiedEmailAndDisplay(settings.email)
                    } else {
                        view.onShowUnverifiedEmail(settings.email)
                        resendVerificationLink(settings.email)
                    }
                },{
                    Timber.e(it)
                    view.onShowErrorAndClose()
                })
    }

    private fun resendVerificationLink(emailAddress: String) {
        settingsDataManager.updateEmail(emailAddress)
                .applySchedulers()
                .addToCompositeDisposable(this)
                .subscribe ({
                    pollForEmailVerified()
                }, {
                    Timber.e(it)
                    view.onShowErrorAndClose()
                })
    }

    fun pollForEmailVerified() {
        Observable.interval(10, TimeUnit.SECONDS, Schedulers.io())
                .flatMap { settingsDataManager.fetchSettings() }
                .applySchedulers()
                .addToCompositeDisposable(this)
                .doOnNext {

                    view.onEnableContinueButton(it.isEmailVerified)

                    if (it.isEmailVerified) {
                        setVerifiedEmailAndDisplay(it.email)
                    }
                }
                .takeUntil { it.isEmailVerified }
                .subscribe {
                    //no-op
                }
    }

    @VisibleForTesting
    fun setVerifiedEmailAndDisplay(verifiedEmail: String) {
        verifiedEmailAddress = verifiedEmail
        view.onShowVerifiedEmail(verifiedEmail)
    }

    fun onContinueClicked(countryCode: String) {
        verifiedEmailAddress?.run {
            createCoinifyAccount(this, countryCode)
                    .applySchedulers()
                    .subscribe ({
                        view.onStartSignUpSuccess()
                    }, {
                        Timber.e(it)
                        view.onShowErrorAndClose()
                    })
        } ?: view.onShowErrorAndClose()
    }

    fun onTermsCheckChanged() {
        view.onEnableContinueButton(!verifiedEmailAddress.isNullOrEmpty())
    }

    /**
     * Creates Coinify account.
     * Saves Coinify metadata.
     * Starts KYC review process and navigates to iSignThis webview.
     *
     * @return [Completable]
     */
    private fun createCoinifyAccount(verifiedEmailAddress: String, countryCode: String?): Observable<KycResponse> {
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
                                countryCode,
                                it
                        ).toObservable()
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
}