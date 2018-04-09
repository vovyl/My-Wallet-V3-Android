package piuk.blockchain.androidbuysell.api

import io.reactivex.Single
import piuk.blockchain.androidbuysell.models.SignUpDetails
import piuk.blockchain.androidbuysell.models.TraderResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url

internal interface Coinify {

    @POST
    fun signUp(
            @Url url: String,
            @Body signUpDetails: SignUpDetails
    ): Single<TraderResponse>

}