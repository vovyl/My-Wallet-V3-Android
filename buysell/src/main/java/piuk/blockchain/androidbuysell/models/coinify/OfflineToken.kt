package piuk.blockchain.androidbuysell.models.coinify

import com.squareup.moshi.Json

/**
 * An offline token is a lifetime token, granted when a user signs up.
 */
internal data class OfflineToken(
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