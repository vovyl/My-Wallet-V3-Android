package com.blockchain.serialization

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.math.BigInteger

class BigIntegerAdapter {

    @FromJson
    fun fromJson(json: String): BigInteger = json.toBigInteger()

    @ToJson
    fun toJson(bigInteger: BigInteger): String = bigInteger.toString()
}