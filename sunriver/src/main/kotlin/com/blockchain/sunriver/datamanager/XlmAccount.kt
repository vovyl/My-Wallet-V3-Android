package com.blockchain.sunriver.datamanager

import com.blockchain.serialization.JsonSerializable

internal data class XlmAccount(
    val publicKey: String,
    val label: String?,
    val archived: Boolean
) : JsonSerializable
