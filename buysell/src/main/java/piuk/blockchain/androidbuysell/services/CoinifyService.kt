package piuk.blockchain.androidbuysell.services

import io.reactivex.Single
import okhttp3.ResponseBody
import piuk.blockchain.androidbuysell.api.COINIFY_BASE
import piuk.blockchain.androidbuysell.api.Coinify
import piuk.blockchain.androidcore.data.rxjava.RxBus
import piuk.blockchain.androidcore.data.rxjava.RxPinning
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class CoinifyService @Inject constructor(@Named("dynamic")retrofit: Retrofit, rxBus: RxBus) {

    private val service: Coinify = retrofit.create(Coinify::class.java)
    private val rxPinning: RxPinning = RxPinning(rxBus)

    internal fun exampleCall() : Single<ResponseBody> = rxPinning.callSingle {
        service.exampleGet(COINIFY_BASE)
    }

}