package com.blockchain.wallet

interface SeedAccess {

    /**
     * The HD Seed which comes from the mnemonic.
     * If it cannot, then it can throw [NoSeedException]
     */
    val hdSeed: ByteArray
}

class NoSeedException : RuntimeException()
