package com.blockchain.sunriver

import info.blockchain.balance.CryptoValue
import java.net.URI
import java.util.regex.Pattern

fun String.isValidXlmAddress(): Boolean = try {
    HorizonKeyPair.createValidatedPublic(this)
    true
} catch (e: InvalidAccountIdException) {
    this.contains("web+stellar")
}

fun String.fromStellarUri(): StellarPayment {
    if (this.contains("web+stellar")) {
        val uri = URI.create(this)
        val pat = Pattern.compile("([^&=]+)=([^&]*)")
        val matcher = pat.matcher(uri.schemeSpecificPart)
        val map = mutableMapOf<String, String>()
        while (matcher.find()) {
            map[matcher.group(1)] = matcher.group(2)
        }

        val amount = map["amount"]?.let { CryptoValue.lumensFromMajor(it.toBigDecimal()) } ?: CryptoValue.ZeroXlm

        return StellarPayment(HorizonKeyPair.createValidatedPublic(map["pay?destination"]!!), amount)
    } else {
        if (this.isValidXlmAddress()) {
            return StellarPayment(HorizonKeyPair.createValidatedPublic(this), CryptoValue.ZeroXlm)
        } else {
            throw IllegalArgumentException("Invalid Stellar address")
        }
    }
}

data class StellarPayment(val public: HorizonKeyPair.Public, val value: CryptoValue)