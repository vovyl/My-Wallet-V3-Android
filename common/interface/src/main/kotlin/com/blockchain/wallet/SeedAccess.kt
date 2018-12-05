package com.blockchain.wallet

import io.reactivex.Maybe

interface SeedAccessWithoutPrompt {

    /**
     * The HD Seeds and master keys which come from the mnemonic.
     * If a second password is required and the wallet is not previously decoded, then it will be empty.
     */
    val seed: Maybe<Seed>

    /**
     * The seed given the pre-validated password. In general you do not want to call this directly, use [SeedAccess]
     * which will call this indirectly after prompting the user on your behalf.
     */
    fun seed(validatedSecondPassword: String): Maybe<Seed>
}

interface SeedAccess : SeedAccessWithoutPrompt {

    /**
     * The HD Seeds and master keys which come from the mnemonic.
     * If a second password is required and not supplied, then it will be empty.
     * If the wallet has been decoded before, there may not be a second prompt, depending on caching lower down.
     */
    val seedPromptIfRequired: Maybe<Seed>

    /**
     * The HD Seeds and master keys which come from the mnemonic.
     * If a second password is not set, there will be no prompt.
     * If a second password is set, even if previously decoded, then there will be a prompt.
     */
    val seedForcePrompt: Maybe<Seed>
}

class Seed(
    val hdSeed: ByteArray,
    val masterKey: ByteArray
)
