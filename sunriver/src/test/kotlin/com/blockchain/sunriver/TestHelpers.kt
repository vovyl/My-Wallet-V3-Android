package com.blockchain.sunriver

import com.nhaarman.mockito_kotlin.argThat
import org.stellar.sdk.KeyPair
import java.util.Arrays

fun keyPairEq(expected: KeyPair) = argThat<KeyPair> {
    keyPairEquality(expected, this)
}

fun keyPairEq(expected: HorizonKeyPair) =
    argThat<HorizonKeyPair> { keyPairEquality(expected.toKeyPair(), this.toKeyPair()) }

private fun keyPairEquality(a: KeyPair, b: KeyPair): Boolean {
    if (a.accountId != b.accountId) return false
    if (a.canSign() != b.canSign()) return false
    if (a.canSign()) {
        if (!Arrays.equals(a.secretSeed, b.secretSeed)) return false
    }
    return true
}
