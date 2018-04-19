package piuk.blockchain.androidbuysell.datamanagers

import io.reactivex.Observable
import io.reactivex.Single
import piuk.blockchain.androidbuysell.models.CoinifyData
import piuk.blockchain.androidbuysell.models.coinify.AuthRequest
import piuk.blockchain.androidbuysell.models.coinify.AuthResponse
import piuk.blockchain.androidbuysell.models.coinify.GrantType
import piuk.blockchain.androidbuysell.models.coinify.KycResponse
import piuk.blockchain.androidbuysell.models.coinify.PaymentMethods
import piuk.blockchain.androidbuysell.models.coinify.Quote
import piuk.blockchain.androidbuysell.models.coinify.QuoteRequest
import piuk.blockchain.androidbuysell.models.coinify.SignUpDetails
import piuk.blockchain.androidbuysell.models.coinify.TraderResponse
import piuk.blockchain.androidbuysell.repositories.AccessTokenStore
import piuk.blockchain.androidbuysell.services.CoinifyService
import piuk.blockchain.androidcore.data.auth.AuthService
import piuk.blockchain.androidcore.data.metadata.MetadataManager
import piuk.blockchain.androidcore.data.walletoptions.WalletOptionsDataManager
import piuk.blockchain.androidcore.injection.PresenterScope
import piuk.blockchain.androidcore.utils.Optional
import piuk.blockchain.androidcore.utils.extensions.applySchedulers
import timber.log.Timber
import javax.inject.Inject

@PresenterScope
class CoinifyDataManager @Inject constructor(
        private val coinifyService: CoinifyService,
        private val authService: AuthService,
        private val walletOptionsDataManager: WalletOptionsDataManager,
        private val accessTokenStore: AccessTokenStore,
        private val buyDataManager: BuyDataManager,
        private val metadataManager: MetadataManager
) {

    /**
     * Accepts a trusted email address, as well as the user's GUID, SharedKey, currency & country codes.
     * Fetches a signed JSON Web Token from the explorer, which is then passed to Coinify and used
     * to sign up a new trader.
     *
     * @param guid The user's GUID.
     * @param sharedKey The user's SharedKey.
     * @param trustedEmail A previously validated email address. Attempting to fetch a signed JWT when
     * the address isn't validated will fail.
     * @param currencyCode The selected currency code, ie "GBP", "USD".
     * @param countryCode The user's selected country code, ie "UK", "USA".
     *
     * @return A [Single] wrapping a [TraderResponse] object, which mostly just contains the details
     * added here.
     */
    fun getEmailTokenAndSignUp(
            guid: String,
            sharedKey: String,
            trustedEmail: String,
            currencyCode: String,
            countryCode: String
    ): Single<TraderResponse> =
            authService.getSignedJwt(guid, sharedKey, "coinify")
                    .flatMap { emailToken ->
                        coinifyService.signUp(
                                signUpDetails = SignUpDetails.basicSignUp(
                                        trustedEmail,
                                        currencyCode,
                                        countryCode,
                                        emailToken
                                )
                        ).doOnSuccess {
                            /* TODO Store this token in metadata on success */
                            Timber.d(it.offlineToken)
                        }
                    }
                    .applySchedulers()

    /**
     * Returns a [TraderResponse] object containing details about the currently authenticated Coinify
     * user, including country code, trade limits etc.
     *
     * @param offlineToken The user's offline token, retrieved from metadata via [CoinifyData.getToken].
     *
     * @return A [TraderResponse] object wrapped in a [Single].
     */
    fun getTrader(offlineToken: String): Single<TraderResponse> =
            authenticate(offlineToken)
                    .flatMap { coinifyService.getTrader(accessToken = it.accessToken) }

    /**
     * Starts the KYC process for an authenticated user and returns a [KycResponse] object,
     * which contains the [KycResponse.redirectUrl] for the iSignThis WebView.
     *
     * @param offlineToken The user's offline token, retrieved from metadata via [CoinifyData.getToken].
     *
     * @return A [KycResponse] wrapped in a [Single].
     */
    fun startKycReview(offlineToken: String): Single<KycResponse> =
            authenticate(offlineToken)
                    .flatMap { coinifyService.startKycReview(accessToken = it.accessToken) }

    /**
     * Returns a [KycResponse] object for an associated KYC review ID. This allows you to get the
     * current status of a user's KYC process.
     *
     * @param offlineToken The user's offline token, retrieved from metadata via [CoinifyData.getToken].
     * @param id The user's associated KYC review ID.
     *
     * @return A [KycResponse] wrapped in a [Single].
     */
    fun getKycReviewStatus(offlineToken: String, id: Int): Single<KycResponse> =
            authenticate(offlineToken)
                    .flatMap {
                        coinifyService.getKycReviewStatus(
                                id = id,
                                accessToken = it.accessToken
                        )
                    }

    /**
     * Returns a [Quote] object containing the exchange rates for the selected currencies. Currencies
     * are ISO_4217 Strings, eg "USD", "BTC". Coinify's API is a little strange, so the rules
     * are as follows:
     *
     * If you're sending sending 1.0 BTC in exchange for GBP, the [baseCurrency] is BTC, the
     * [quoteCurrency] is GBP and the [baseAmount] value must be negative. The [Quote.quoteAmount]
     * received in return is positive, because that's the amount you're receiving.
     *
     * If you are sending BTC in exchange for GBP and want £100 worth, [baseCurrency] is GBP,
     * [quoteCurrency] is BTC and the [baseAmount] value must be positive. The corresponding
     * [Quote.baseAmount] returned will be negative, because that's the amount you'll need to send.
     *
     * @param offlineToken The user's offline token, retrieved from metadata via [CoinifyData.getToken].
     * @param baseAmount The amount of currency you wish to convert, eg 500.00.
     * @param baseCurrency The currency you wish to send to Coinify.
     * @param quoteCurrency The currency you wish to receive from Coinify.
     *
     * @return A [Quote] object wrapped in a [Single].
     *
     * @see [https://en.wikipedia.org/wiki/ISO_4217].
     */
    fun getQuote(
            offlineToken: String,
            baseAmount: Double,
            baseCurrency: String,
            quoteCurrency: String
    ): Single<Quote> =
            authenticate(offlineToken)
                    .flatMap {
                        coinifyService.getQuote(
                                quoteRequest = QuoteRequest(
                                        baseCurrency,
                                        quoteCurrency,
                                        baseAmount
                                ),
                                accessToken = it.accessToken
                        )
                    }

    /**
     * Returns a steam of [PaymentMethods] objects - in practise there will be 2-4 objects streamed.
     * Currencies parameters are ISO_4217 Strings, eg "USD", "BTC".
     *
     * @param offlineToken The user's offline token, retrieved from metadata via [CoinifyData.getToken].
     * @param inCurrency The currency you wish to send to Coinify.
     * @param outCurrency The currency you wish to receive from Coinify.
     *
     * @return A stream of [PaymentMethods] objects wrapped in an [Observable].
     *
     * @see [https://en.wikipedia.org/wiki/ISO_4217].
     */
    fun getPaymentMethods(
            offlineToken: String,
            inCurrency: String,
            outCurrency: String
    ): Observable<PaymentMethods> =
            authenticate(offlineToken)
                    .flatMap {
                        coinifyService.getPaymentMethods(
                                inCurrency = inCurrency,
                                outCurrency = outCurrency,
                                accessToken = it.accessToken
                        )
                    }.flattenAsObservable { it }

    /**
     * Authenticates the user with Coinify if no token or an outdated token is stored. Returns the
     * current valid token if already stored.
     *
     * @param offlineToken The user's offline token, retrieved from metadata via [CoinifyData.getToken].
     *
     * @return A valid [AuthResponse] object wrapped in an [Single].
     */
    private fun authenticate(offlineToken: String): Single<AuthResponse> =
            if (accessTokenStore.requiresRefresh()) {
                coinifyService.auth(authRequest = AuthRequest(GrantType.OfflineToken, offlineToken))
                        .flatMapObservable(accessTokenStore::store)
            } else {
                accessTokenStore.getAccessToken()
                        .map { (it as Optional.Some).element }
            }.singleOrError()

}