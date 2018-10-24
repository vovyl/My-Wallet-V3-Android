package com.blockchain.sunriver

import com.blockchain.sunriver.derivation.deriveXlmAccountKeyPair
import com.blockchain.wallet.SeedAccess
import io.reactivex.Maybe
import io.reactivex.schedulers.Schedulers

internal class XlmSecretAccess(private val seedAccess: SeedAccess) {

    /**
     * Searches for an account that matches the public.
     * Forces a second password prompt when second password is set.
     */
    fun getPrivate(forPublic: HorizonKeyPair.Public): Maybe<HorizonKeyPair.Private> =
        seedAccess
            .seedForcePrompt
            .observeOn(Schedulers.computation())
            .flatMap { seed ->
                for (i in 0..20) {
                    val keyPair = deriveXlmAccountKeyPair(seed.hdSeed, i)
                    if (keyPair.accountId == forPublic.accountId) {
                        return@flatMap Maybe.just(keyPair)
                    }
                }
                Maybe.empty<HorizonKeyPair.Private>()
            }
}
