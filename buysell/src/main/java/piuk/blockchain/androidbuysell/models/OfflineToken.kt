package piuk.blockchain.androidbuysell.models

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * An offline token is a lifetime token, granted when a user signs up.
 */
internal data class OfflineToken(
        @JsonProperty("grant_type") val grantType: GrantType,
        @JsonProperty("offline_token") val offlineToken: String
)

internal enum class GrantType(val type: String) {
    OfflineToken("offline_token"),
    RefreshToken("refresh_token"),
    Password("password");

    override fun toString(): String {
        return type
    }
}