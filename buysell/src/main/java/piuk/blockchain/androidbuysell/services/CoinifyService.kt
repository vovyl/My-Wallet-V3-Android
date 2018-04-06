package piuk.blockchain.androidbuysell.services

import io.reactivex.Single
import piuk.blockchain.androidbuysell.api.COINIFY_BASE
import piuk.blockchain.androidbuysell.api.Coinify
import piuk.blockchain.androidbuysell.models.SignUpDetails
import piuk.blockchain.androidbuysell.models.TraderResponse
import piuk.blockchain.androidcore.data.rxjava.RxBus
import piuk.blockchain.androidcore.data.rxjava.RxPinning
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class CoinifyService @Inject constructor(@Named("dynamic") retrofit: Retrofit, rxBus: RxBus) {

    private val service: Coinify = retrofit.create(Coinify::class.java)
    private val rxPinning: RxPinning = RxPinning(rxBus)

    internal fun signup(signUpDetails: SignUpDetails): Single<TraderResponse> =
            rxPinning.callSingle {
                service.signup(COINIFY_BASE, signUpDetails)
            }

}