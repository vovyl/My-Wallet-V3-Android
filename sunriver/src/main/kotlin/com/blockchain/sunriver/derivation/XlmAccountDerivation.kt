package com.blockchain.sunriver.derivation

import com.blockchain.sunriver.HorizonKeyPair
import com.blockchain.sunriver.toHorizonKeyPair
import org.stellar.sdk.KeyPair

/**
 * Derives an account from the hd seed.
 * The account is at the path m/44'/148'/account' (starting at the Ed25519 root).
 */
internal fun deriveXlmAccountKeyPair(
    hdSeed: ByteArray,
    account: Int = 0
) =
    KeyPair.fromBip39Seed(hdSeed, account)
        .toHorizonKeyPair() as HorizonKeyPair.Private
