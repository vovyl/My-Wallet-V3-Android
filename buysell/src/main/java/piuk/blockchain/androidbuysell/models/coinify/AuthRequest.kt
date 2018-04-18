package piuk.blockchain.androidbuysell.models.coinify

import com.squareup.moshi.Json

/**
 * An offline token is a lifetime token, granted when a user signs up. It is used
 * to get a temporary access token from the Coinfy endpoint.
 */
internal data class AuthRequest(
        @field:Json(name = "grant_type") val grantType: GrantType,
        @field:Json(name = "offline_token") val offlineToken: String
)

internal enum class GrantType(val type: String) {
    OfflineToken("offline_token"),
    RefreshToken("refresh_token"),
    Password("password");

    override fun toString(): String {
        return type
    }
}

data class AuthResponse(
        @field:Json(name = "access_token") val accessToken: String,
        @field:Json(name = "token_type") val tokenType: String,
        // Expiry time in seconds, usually 1200
        @field:Json(name = "expires_in") val expiresIn: Int,
        @field:Json(name = "refresh_token") val refreshToken: String? = null,
        val creationTime: Long = System.currentTimeMillis() / 1000
)