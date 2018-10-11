package com.blockchain.sunriver.derivation

import com.blockchain.sunriver.HorizonKeyPair
import com.blockchain.sunriver.toHorizonKeyPair
import com.github.orogvany.bip32.Network
import com.github.orogvany.bip32.wallet.CoinType
import com.github.orogvany.bip32.wallet.HdKeyGenerator
import org.bitcoinj.crypto.MnemonicCode
import org.stellar.sdk.KeyPair

/**
 * Derives an account from the mnemonic.
 * The account is at the path m/44'/148'/account' on the Ed25519 Curve
 */
fun deriveXlmAccountKeyPair(
    mnemonic: String,
    passphrase: String,
    account: Int = 0
) =
    deriveXlmAccountKeyPair(
        MnemonicCode.toSeed(
            mnemonic.split(" "), passphrase
        ), account
    )

/**
 * Derives an account from the hd seed.
 * The account is at the path m/44'/148'/account' on the Ed25519 Curve
 */
fun deriveXlmAccountKeyPair(
    hdSeed: ByteArray,
    account: Int = 0
): HorizonKeyPair.Private {
    val hdKeyGenerator = HdKeyGenerator()
    return hdSeed
        .let {
            hdKeyGenerator.getAddressFromSeed(
                it,
                Network.mainnet,
                CoinType.semux
            )
        }
        .let { hdKeyGenerator.getAddress(it, 44, true) }
        .let { hdKeyGenerator.getAddress(it, 148, true) }
        .let { hdKeyGenerator.getAddress(it, account.toLong(), true) }
        .let {
            KeyPair.fromSecretSeed(it.privateKey.privateKey)
                .toHorizonKeyPair() as HorizonKeyPair.Private
        }
}
