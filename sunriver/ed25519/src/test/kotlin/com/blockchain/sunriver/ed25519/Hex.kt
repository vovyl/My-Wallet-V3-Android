package com.blockchain.sunriver.ed25519

import java.math.BigInteger

fun ByteArray.toHex(): String {
    val bi = BigInteger(1, this)
    val hex = bi.toString(16)
    val paddingLength = this.size * 2 - hex.length
    return if (paddingLength > 0) {
        String.format("%0${paddingLength}d", 0) + hex
    } else {
        hex
    }
}

fun String.hexToBytes() =
    ByteArray(length / 2).also { result ->
        result.indices.forEach { i ->
            result[i] = (parseHex(this[i * 2]) shl 4 or parseHex(
                this[i * 2 + 1]
            )).toByte()
        }
    }

private fun parseHex(c: Char) =
    when (c) {
        in '0'..'9' -> c - '0'
        in 'a'..'f' -> c - 'a' + 10
        in 'A'..'F' -> c - 'A' + 10
        else -> throw RuntimeException("Invalid hex char '$c'")
    }
