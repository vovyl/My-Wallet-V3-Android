package com.blockchain.sunriver

import info.blockchain.balance.CryptoValue
import java.net.URI
import java.util.regex.Pattern

fun String.isValidXlmQr(): Boolean = tryFromStellarUri() != null

fun String.tryFromStellarUri(): StellarPayment? = try {
    fromStellarUri()
} catch (e: Exception) {
    null
}

fun String.fromStellarUri(): StellarPayment = if (this.contains("web+stellar")) {
    val uri = URI.create(this)
    val pat = Pattern.compile("([^&=]+)=([^&]*)")
    val matcher = pat.matcher(uri.schemeSpecificPart)
    val map = mutableMapOf<String, String>()
    while (matcher.find()) {
        map[matcher.group(1)] = matcher.group(2)
    }

    val amount = map["amount"]?.let { CryptoValue.lumensFromMajor(it.toBigDecimal()) } ?: CryptoValue.ZeroXlm

    StellarPayment(HorizonKeyPair.createValidatedPublic(map["pay?destination"]!!), amount)
} else {
    StellarPayment(HorizonKeyPair.createValidatedPublic(this), CryptoValue.ZeroXlm)
}

data class StellarPayment(val public: HorizonKeyPair.Public, val value: CryptoValue)