package piuk.blockchain.androidbuysell.models.coinify.exceptions

import io.reactivex.Single
import retrofit2.HttpException

internal fun <T> Single<T>.wrapErrorMessage(): Single<T> = this.onErrorResumeNext {
    if (it is HttpException) {
        Single.error(CoinifyApiException.fromResponseBody(it.response()))
    } else {
        Single.error(it)
    }
}