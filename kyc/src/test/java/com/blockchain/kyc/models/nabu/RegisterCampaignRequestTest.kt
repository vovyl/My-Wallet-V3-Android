package com.blockchain.kyc.models.nabu

import com.blockchain.serialization.JsonSerializable
import com.blockchain.testutils.`should be assignable from`
import org.junit.Test

class RegisterCampaignRequestTest {

    @Test
    fun `ensure is JsonSerializable for ProGuard`() {
        JsonSerializable::class.`should be assignable from`(RegisterCampaignRequest::class)
    }
}