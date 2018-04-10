package piuk.blockchain.androidbuysell.datamanagers

import io.reactivex.Single
import piuk.blockchain.androidbuysell.models.coinify.SignUpDetails
import piuk.blockchain.androidbuysell.models.coinify.TraderResponse
import piuk.blockchain.androidbuysell.services.CoinifyService
import piuk.blockchain.androidcore.data.auth.AuthService
import piuk.blockchain.androidcore.data.walletoptions.WalletOptionsDataManager
import piuk.blockchain.androidcore.injection.PresenterScope
import piuk.blockchain.androidcore.utils.extensions.applySchedulers
import timber.log.Timber
import javax.inject.Inject

@PresenterScope
class BuySellDataManager @Inject constructor(
        private val coinifyService: CoinifyService,
        private val authService: AuthService,
        private val walletOptionsDataManager: WalletOptionsDataManager
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

}