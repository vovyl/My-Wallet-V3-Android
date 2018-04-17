package piuk.blockchain.androidbuysell.services

import io.reactivex.Single
import piuk.blockchain.androidbuysell.api.COINIFY_LIVE_BASE
import piuk.blockchain.androidbuysell.api.Coinify
import piuk.blockchain.androidbuysell.api.PATH_COINFY_AUTH
import piuk.blockchain.androidbuysell.api.PATH_COINFY_GET_TRADER
import piuk.blockchain.androidbuysell.api.PATH_COINFY_PREP_KYC
import piuk.blockchain.androidbuysell.api.PATH_COINFY_SIGNUP_TRADER
import piuk.blockchain.androidbuysell.api.PATH_COINFY_TRADES_PAYMENT_METHODS
import piuk.blockchain.androidbuysell.api.PATH_COINFY_TRADES_QUOTE
import piuk.blockchain.androidbuysell.models.coinify.AuthRequest
import piuk.blockchain.androidbuysell.models.coinify.AuthResponse
import piuk.blockchain.androidbuysell.models.coinify.KycRequest
import piuk.blockchain.androidbuysell.models.coinify.KycResponse
import piuk.blockchain.androidbuysell.models.coinify.PaymentMethods
import piuk.blockchain.androidbuysell.models.coinify.Quote
import piuk.blockchain.androidbuysell.models.coinify.QuoteRequest
import piuk.blockchain.androidbuysell.models.coinify.SignUpDetails
import piuk.blockchain.androidbuysell.models.coinify.TraderResponse
import piuk.blockchain.androidcore.data.rxjava.RxBus
import piuk.blockchain.androidcore.data.rxjava.RxPinning
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class CoinifyService @Inject constructor(@Named("kotlin") retrofit: Retrofit, rxBus: RxBus) {

    private val service: Coinify = retrofit.create(Coinify::class.java)
    private val rxPinning: RxPinning = RxPinning(rxBus)
    // TODO: Allow switching of base URL
    private val baseUrl: String
        get() = COINIFY_LIVE_BASE

    internal fun signUp(
            path: String = "$baseUrl$PATH_COINFY_SIGNUP_TRADER",
            signUpDetails: SignUpDetails
    ): Single<TraderResponse> = rxPinning.callSingle {
        service.signUp(path, signUpDetails)
    }

    internal fun getTrader(
            path: String = "$baseUrl$PATH_COINFY_GET_TRADER",
            accessToken: String
    ): Single<TraderResponse> = rxPinning.callSingle {
        service.getTrader(path, getFormattedToken(accessToken))
    }

    internal fun auth(
            path: String = "$baseUrl$PATH_COINFY_AUTH",
            authRequest: AuthRequest
    ): Single<AuthResponse> = rxPinning.callSingle {
        service.auth(path, authRequest)
    }

    internal fun getKycReview(
            path: String = "$baseUrl$PATH_COINFY_PREP_KYC",
            redirectUrl: String,
            accessToken: String
    ): Single<KycResponse> = rxPinning.callSingle {
        service.getKycReview(path, KycRequest(redirectUrl), getFormattedToken(accessToken))
    }

    internal fun getQuote(
            path: String = "$baseUrl$PATH_COINFY_TRADES_QUOTE",
            quoteRequest: QuoteRequest,
            accessToken: String
    ): Single<Quote> = rxPinning.callSingle {
        service.getQuote(path, quoteRequest, getFormattedToken(accessToken))
    }

    internal fun getPaymentMethods(
            path: String = "$baseUrl$PATH_COINFY_TRADES_PAYMENT_METHODS",
            inCurrency: String,
            outCurrency: String,
            accessToken: String
    ): Single<List<PaymentMethods>> = rxPinning.callSingle {
        service.getPaymentMethods(path, inCurrency, outCurrency, getFormattedToken(accessToken))
    }

    private fun getFormattedToken(accessToken: String) = "Bearer $accessToken"

}