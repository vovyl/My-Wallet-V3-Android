package piuk.blockchain.androidbuysell.models.coinify

import com.squareup.moshi.Json

data class TraderResponse(
        val trader: Trader,
        val offlineToken: String
)

data class Trader(
        val id: Int,
        val defaultCurrency: String,
        val email: String,
        val profile: Profile,
        val level: Level? = null
)

data class Profile(
        val address: Address,
        val name: String? = null,
        val mobile: Mobile? = null
)

data class Address(
        @field:Json(name = "country") val countryCode: String,
        val street: String?,
        val zipcode: String?,
        val city: String?,
        val state: String?
)

data class Mobile(val countryCode: String, val number: String)

data class Level(
        val id: Int,
        val name: String,
        val currency: String,
        val feePercentage: Double,
        val limits: Limits? = null
)

data class Limits(val card: Card)

data class Card(@field:Json(name = "in") val inX: In)

data class In(val daily: Double, val yearly: Double)