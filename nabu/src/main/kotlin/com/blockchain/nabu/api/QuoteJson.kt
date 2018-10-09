package com.blockchain.nabu.api

import com.blockchain.serialization.JsonSerializable
import java.math.BigDecimal

data class QuoteJson(
    val pair: String,
    val fiatCurrency: String,
    val fix: String,
    val volume: BigDecimal,
    val currencyRatio: CurrencyRatio
) : JsonSerializable

data class CurrencyRatio(
    val base: CryptoAndFiat,
    val counter: CryptoAndFiat,
    val baseToFiatRate: BigDecimal,
    val baseToCounterRate: BigDecimal,
    val counterToBaseRate: BigDecimal,
    val counterToFiatRate: BigDecimal
) : JsonSerializable

data class CryptoAndFiat(
    val fiat: Value,
    val crypto: Value
) : JsonSerializable

data class Value(
    val symbol: String,
    val value: BigDecimal
) : JsonSerializable
