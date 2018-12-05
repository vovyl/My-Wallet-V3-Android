package com.blockchain.sunriver.datamanager

import com.blockchain.serialization.JsonSerializable
import com.squareup.moshi.Json

internal data class XlmMetaData(

    @field:Json(name = "default_account_idx")
    val defaultAccountIndex: Int,

    val accounts: List<XlmAccount>?,

    @field:Json(name = "tx_notes")
    val transactionNotes: Map<String, String>?
) : JsonSerializable {

    companion object {

        const val MetaDataType = 11
    }
}

internal fun XlmMetaData.default() = accounts!![defaultAccountIndex]
