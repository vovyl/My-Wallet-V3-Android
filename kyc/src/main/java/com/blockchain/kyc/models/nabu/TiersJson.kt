package com.blockchain.kyc.models.nabu

import com.blockchain.serialization.JsonSerializable
import info.blockchain.balance.FiatValue
import java.math.BigDecimal

data class TiersJson(
    val tiers: List<TierJson>
) : JsonSerializable

data class TierJson(
    val index: Int,
    val name: String,
    val state: KycTierState,
    val limits: LimitsJson
) : JsonSerializable

data class LimitsJson(
    val currency: String,
    val daily: BigDecimal?,
    val annual: BigDecimal?
) : JsonSerializable {

    val dailyFiat: FiatValue? = daily?.let { FiatValue.fromMajor(currency, it) }

    val annualFiat: FiatValue? = annual?.let { FiatValue.fromMajor(currency, it) }
}

enum class KycTierState {
    None,
    Rejected,
    Pending,
    Verified
}
