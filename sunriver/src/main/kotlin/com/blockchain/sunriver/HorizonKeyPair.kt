package com.blockchain.sunriver

import org.stellar.sdk.KeyPair

sealed class HorizonKeyPair(val accountId: String) {

    data class Public(private val _accountId: String) : HorizonKeyPair(_accountId) {
        override fun neuter() = this
    }

    data class Private(private val _accountId: String, val secret: CharArray) : HorizonKeyPair(_accountId) {
        override fun neuter() = Public(accountId)
    }

    abstract fun neuter(): Public

    companion object {

        /**
         * Throws [InvalidAccountIdException] when accountId is not valid
         */
        fun createValidatedPublic(accountId: String): Public {
            val keyPair: KeyPair
            try {
                keyPair = KeyPair.fromAccountId(accountId)
            } catch (e: Exception) {
                throw InvalidAccountIdException(e.message)
            }
            return keyPair.toHorizonKeyPair().neuter()
        }
    }
}

class InvalidAccountIdException(message: String?) : RuntimeException("Invalid Account Id, $message")

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
