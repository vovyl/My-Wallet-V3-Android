package piuk.blockchain.androidbuysell.api

import io.reactivex.Single
import piuk.blockchain.androidbuysell.models.SignUpDetails
import piuk.blockchain.androidbuysell.models.TraderResponse
import retrofit2.http.POST
import retrofit2.http.Url

internal interface Coinify {

    @POST("/$PATH_COINFY_SIGNUP_TRADER")
    fun signup(
            @Url url: String,
            signUpDetails: SignUpDetails
    ): Single<TraderResponse>

}