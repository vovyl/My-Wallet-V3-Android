package com.blockchain.sunriver.models

import com.blockchain.sunriver.HorizonKeyPair
import info.blockchain.balance.CryptoValue

data class XlmTransaction(
    val timeStamp: String,
    val value: CryptoValue,
    // Tech debt AND-1663 Repeated Hardcoded fee
    val fee: CryptoValue = CryptoValue.lumensFromStroop(100.toBigInteger()),
    val hash: String,
    val to: HorizonKeyPair.Public,
    val from: HorizonKeyPair.Public
) {
    val accountDelta: CryptoValue
        get() =
            if (value.isPositive) {
                value
            } else {
                value - fee
            }
}
