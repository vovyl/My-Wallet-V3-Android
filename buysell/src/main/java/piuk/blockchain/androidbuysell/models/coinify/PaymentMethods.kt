package piuk.blockchain.androidbuysell.models.coinify

import com.squareup.moshi.FromJson
import com.squareup.moshi.Json
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.ToJson
import piuk.blockchain.androidbuysell.models.coinify.ReasonCodes.Companion.forcedDelay
import piuk.blockchain.androidbuysell.models.coinify.ReasonCodes.Companion.limitsExceeded
import piuk.blockchain.androidbuysell.models.coinify.ReasonCodes.Companion.tradeInProgress

data class PaymentMethods(
        val outCurrency: String,
        val outCurrencies: List<String>,
        val inCurrency: String,
        val inCurrencies: List<String>,
        val inMedium: String,
        val outMedium: String,
        val name: String,
        val minimumInAmounts: MinimumInAmounts,
        val inFixedFees: InFixedFees,
        val inPercentageFee: Double,
        val outFixedFees: OutFixedFees,
        val outPercentageFee: Int,
        val canTrade: Boolean,
        val cannotTradeReasons: List<CannotTradeReason>?
)

data class InFixedFees(
        @Json(name = "DKK") val dkk: Int,
        @Json(name = "EUR") val eur: Int,
        @Json(name = "USD") val usd: Int,
        @Json(name = "GBP") val gbp: Int
)

data class MinimumInAmounts(
        @Json(name = "DKK") val dkk: Double,
        @Json(name = "EUR") val eur: Int,
        @Json(name = "USD") val usd: Double,
        @Json(name = "GBP") val gbp: Double
)

data class OutFixedFees(
        @get:Json(name = "BTC") val btc: Double
)

sealed class CannotTradeReason

/**
 * Trader must wait until a specific time before creating trade.
 *
 * @param delayEnd ISO-8601 timestamp for when the delay is over.
 */
data class ForcedDelay(
        val reasonCode: String = forcedDelay,
        val delayEnd: String
) : CannotTradeReason()

/**
 * Trader must wait until a specific trade has completed.
 *
 * @param tradeId ID of trade that must be completed
 */
data class TradeInProgress(
        val reasonCode: String = tradeInProgress,
        val tradeId: Int
) : CannotTradeReason()

/**
 * Creating trade would exceed the trader’s limits.
 */
data class LimitsExceeded(val reasonCode: String = limitsExceeded) :
    CannotTradeReason()

/**
 * Contains every possible value for custom type adapter [CannotTradeReasonAdapter].
 */
data class CannotTradeReasonJson(
        val reasonCode: String,
        val delayEnd: String? = null,
        val tradeId: Int? = null
)

@Suppress("unused")
class CannotTradeReasonAdapter {

    @FromJson
    fun fromJson(json: CannotTradeReasonJson): CannotTradeReason = when (json.reasonCode) {
        forcedDelay -> ForcedDelay(json.reasonCode, json.delayEnd!!)
        tradeInProgress -> TradeInProgress(json.reasonCode, json.tradeId!!)
        limitsExceeded -> LimitsExceeded(json.reasonCode)
        else -> throw JsonDataException("Unknown CannotTradeReason ${json.reasonCode}, unsupported data type")
    }

    @ToJson
    fun toJson(cannotTradeReason: CannotTradeReason): CannotTradeReasonJson =
            when (cannotTradeReason) {
                is ForcedDelay -> CannotTradeReasonJson(
                        cannotTradeReason.reasonCode,
                        delayEnd = cannotTradeReason.delayEnd
                )
                is TradeInProgress -> CannotTradeReasonJson(
                        cannotTradeReason.reasonCode,
                        tradeId = cannotTradeReason.tradeId
                )
                is LimitsExceeded -> CannotTradeReasonJson(
                        cannotTradeReason.reasonCode
                )
            }
}

class ReasonCodes {

    companion object {
        internal const val forcedDelay = "forced_delay"
        internal const val tradeInProgress = "trade_in_progress"
        internal const val limitsExceeded = "limits_exceeded"
    }

}