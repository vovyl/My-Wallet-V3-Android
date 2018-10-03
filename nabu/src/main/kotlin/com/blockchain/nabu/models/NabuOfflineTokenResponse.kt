package com.blockchain.nabu.models

import com.blockchain.nabu.metadata.NabuCredentialsMetadata

data class NabuOfflineTokenRequest(
    val jwt: String
)

data class NabuOfflineTokenResponse(
    val userId: String,
    val token: String
) {
    val authHeader
        get() = "Bearer $token"
}

fun NabuOfflineTokenResponse.mapToMetadata(): NabuCredentialsMetadata =
    NabuCredentialsMetadata(this.userId, this.token)

fun NabuCredentialsMetadata.mapFromMetadata(): NabuOfflineTokenResponse =
    NabuOfflineTokenResponse(this.userId, this.lifetimeToken)
