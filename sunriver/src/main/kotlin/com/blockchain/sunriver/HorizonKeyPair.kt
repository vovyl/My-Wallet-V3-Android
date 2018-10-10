package com.blockchain.sunriver

import org.stellar.sdk.KeyPair

sealed class HorizonKeyPair(val accountId: String) {

    class Public(accountId: String) : HorizonKeyPair(accountId) {
        override fun neuter() = this
    }

    class Private(accountId: String, val secret: CharArray) : HorizonKeyPair(accountId) {
        override fun neuter() = Public(accountId)
    }

    abstract fun neuter(): Public
}

internal fun KeyPair.toHorizonKeyPair() =
    if (canSign()) {
        HorizonKeyPair.Private(accountId, secretSeed)
    } else {
        HorizonKeyPair.Public(accountId)
    }

internal fun HorizonKeyPair.toKeyPair() =
    when (this) {
        is HorizonKeyPair.Public -> KeyPair.fromAccountId(accountId)
    // TODO("AND-1500") There is an issue calling the overload we want, the char array one. A test fails.
        is HorizonKeyPair.Private -> KeyPair.fromSecretSeed(String(secret))
    }
