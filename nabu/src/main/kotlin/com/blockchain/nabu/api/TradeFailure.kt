package com.blockchain.nabu.api

import com.blockchain.serialization.JsonSerializable

data class TradeFailureJson(
    val txHash: String?,
    val failureReason: FailureReasonJson?
) : JsonSerializable

data class FailureReasonJson(
    val message: String
) : JsonSerializable
