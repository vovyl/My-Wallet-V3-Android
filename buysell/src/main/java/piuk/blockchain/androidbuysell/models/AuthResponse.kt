package piuk.blockchain.androidbuysell.models
import com.fasterxml.jackson.annotation.JsonProperty

data class AuthResponse(
		@JsonProperty("access_token") val accessToken: String,
		@JsonProperty("token_type") val tokenType: String,
		// Lifetime of the access token in seconds, usually 20 * 60
		@JsonProperty("expires_in") val expiresIn: Int,
		@JsonProperty("refresh_token") val refreshToken: String?
)