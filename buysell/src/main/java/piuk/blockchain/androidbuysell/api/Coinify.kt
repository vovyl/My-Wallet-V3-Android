package piuk.blockchain.androidbuysell.api

import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Url

internal interface Coinify {

    @GET
    fun exampleGet(@Url url: String) : Single<ResponseBody>

}