package com.blockchain.sunriver.derivation

import com.blockchain.sunriver.HorizonKeyPair
import com.blockchain.sunriver.ed25519.deriveEd25519PrivateKey
import com.blockchain.sunriver.toHorizonKeyPair
import org.stellar.sdk.KeyPair

/**
 * Derives an account from the hd seed.
 * The account is at the path m/44'/148'/account' on the Ed25519 Curve
 */
fun deriveXlmAccountKeyPair(
    hdSeed: ByteArray,
    account: Int = 0
) =
    KeyPair.fromSecretSeed(
        deriveEd25519PrivateKey(hdSeed, 44, 148, account)
    ).toHorizonKeyPair() as HorizonKeyPair.Private
