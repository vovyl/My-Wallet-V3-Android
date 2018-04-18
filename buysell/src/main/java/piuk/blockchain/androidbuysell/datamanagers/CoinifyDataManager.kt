package piuk.blockchain.androidbuysell.datamanagers

import io.reactivex.Observable
import io.reactivex.Single
import piuk.blockchain.androidbuysell.models.CoinifyData
import piuk.blockchain.androidbuysell.models.coinify.AuthRequest
import piuk.blockchain.androidbuysell.models.coinify.AuthResponse
import piuk.blockchain.androidbuysell.models.coinify.GrantType
import piuk.blockchain.androidbuysell.models.coinify.PaymentMethods
import piuk.blockchain.androidbuysell.models.coinify.Quote
import piuk.blockchain.androidbuysell.models.coinify.QuoteRequest
import piuk.blockchain.androidbuysell.models.coinify.SignUpDetails
import piuk.blockchain.androidbuysell.models.coinify.TraderResponse
import piuk.blockchain.androidbuysell.repositories.AccessTokenStore
import piuk.blockchain.androidbuysell.services.CoinifyService
import piuk.blockchain.androidcore.data.auth.AuthService
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
        private val buyDataManager: BuyDataManager
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
     * Returns a [Quote] object containing the exchange rates for the selected currencies. Currencies
     * are ISO_4217 Strings, eg "USD", "BTC".
     *
     * @param offlineToken The user's offline token, retrieved from metadata via [CoinifyData.getToken].
     * @param inCurrency The currency you wish to send to Coinify.
     * @param outCurrency The currency you wish to receive from Coinify.
     *
     * @return A [Quote] object wrapped in a [Single].
     *
     * @see [https://en.wikipedia.org/wiki/ISO_4217].
     */
    fun getQuote(offlineToken: String, inCurrency: String, outCurrency: String): Single<Quote> =
            authenticate(offlineToken)
                    .flatMap {
                        coinifyService.getQuote(
                                quoteRequest = QuoteRequest(inCurrency, outCurrency),
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