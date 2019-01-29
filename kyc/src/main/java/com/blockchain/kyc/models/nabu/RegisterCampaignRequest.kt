package com.blockchain.kyc.models.nabu

import com.blockchain.serialization.JsonSerializable

data class RegisterCampaignRequest(
    val data: Map<String, String>,
    val newUser: Boolean
) : JsonSerializable {

    companion object {

        fun registerSunriver(
            accountId: String,
            newUser: Boolean
        ) = RegisterCampaignRequest(
            mapOf(
                "x-campaign-address" to accountId
            ),
            newUser
        )
    }
}