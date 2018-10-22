package com.blockchain.wallet

import io.reactivex.Maybe

interface SeedAccess {

    /**
     * The HD Seeds and master keys which come from the mnemonic.
     * If a second password is required and not supplied, then it will be empty.
     */
    val seed: Maybe<Seed>
}

class Seed(
    val hdSeed: ByteArray,
    val masterKey: ByteArray
)
