package info.blockchain.wallet.api.dust

import info.blockchain.wallet.api.dust.data.DustInput
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface DustApi {

    @GET("{currency}/dust")
    fun getDust(
        @Path("currency") currency: String,
        @Query("api_code") apiCode: String
    ): Single<DustInput>
}