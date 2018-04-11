package piuk.blockchain.androidbuysell.services

import io.reactivex.Single
import piuk.blockchain.androidbuysell.api.COINIFY_BASE
import piuk.blockchain.androidbuysell.api.Coinify
import piuk.blockchain.androidbuysell.api.PATH_COINFY_SIGNUP_TRADER
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

    internal fun signUp(
            path: String = "$COINIFY_BASE$PATH_COINFY_SIGNUP_TRADER",
            signUpDetails: SignUpDetails
    ): Single<TraderResponse> =
            rxPinning.callSingle { service.signUp(path, signUpDetails) }

}