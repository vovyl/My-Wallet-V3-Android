package piuk.blockchain.androidcore.data.ethereum

import info.blockchain.wallet.ethereum.EthereumAccount
import org.bitcoinj.core.ECKey
import org.bitcoinj.crypto.DeterministicKey
import piuk.blockchain.androidcore.utils.annotations.Mockable

/**
 * This class is simply for making [EthereumAccount.deriveECKey] mockable for testing.
 */
@Mockable
class EthereumAccountWrapper {

    fun deriveECKey(masterKey: DeterministicKey, accountIndex: Int): ECKey =
            EthereumAccount.deriveECKey(masterKey, accountIndex)

}