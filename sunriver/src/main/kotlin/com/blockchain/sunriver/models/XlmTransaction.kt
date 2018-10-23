package com.blockchain.sunriver.models

import com.blockchain.sunriver.HorizonKeyPair
import info.blockchain.balance.CryptoValue

data class XlmTransaction(
    val timeStamp: String,
    val total: CryptoValue,
    val hash: String,
    val to: HorizonKeyPair.Public,
    val from: HorizonKeyPair.Public
)