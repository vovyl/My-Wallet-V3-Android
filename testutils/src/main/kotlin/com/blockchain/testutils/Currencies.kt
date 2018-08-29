package com.blockchain.testutils

import info.blockchain.balance.CryptoValue
import info.blockchain.balance.FiatValue
import java.math.BigDecimal

fun Number.gbp() = FiatValue.fromMajor("GBP", numberToBigDecimal())

fun Number.usd() = FiatValue.fromMajor("USD", numberToBigDecimal())

fun Number.cad() = FiatValue.fromMajor("CAD", numberToBigDecimal())

private fun Number.numberToBigDecimal(): BigDecimal =
    when (this) {
        is Double -> toBigDecimal()
        is Int -> toBigDecimal()
        is Long -> toBigDecimal()
        else -> throw NotImplementedError(this.javaClass.name)
    }

fun Double.bitcoin() = CryptoValue.bitcoinFromMajor(toBigDecimal())
fun Double.ether() = CryptoValue.etherFromMajor(toBigDecimal())
fun Double.bitcoinCash() = CryptoValue.bitcoinCashFromMajor(toBigDecimal())
