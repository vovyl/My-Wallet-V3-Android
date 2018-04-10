package piuk.blockchain.androidbuysell.models.coinify

import com.squareup.moshi.Json

data class AuthResponse(
        @field:Json(name = "access_token") val accessToken: String,
        @field:Json(name = "token_type") val tokenType: String,
        // Lifetime of the access token in seconds, usually 20 * 60
        @field:Json(name = "expires_in") val expiresIn: Int,
        @field:Json(name = "refresh_token") val refreshToken: String? = null
)