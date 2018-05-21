package piuk.blockchain.androidbuysell.models.coinify

import com.squareup.moshi.Json

data class BankAccount(
        val id: Int,
        val account: Account,
        val bank: Bank,
        val holder: Holder,
        @Json(name = "update_time") val updateTime: String,
        @Json(name = "create_time") val createTime: String
)