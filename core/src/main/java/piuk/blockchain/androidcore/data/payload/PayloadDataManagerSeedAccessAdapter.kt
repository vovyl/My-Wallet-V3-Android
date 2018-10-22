package piuk.blockchain.androidcore.data.payload

import com.blockchain.wallet.SeedAccess
import com.blockchain.wallet.Seed
import info.blockchain.wallet.exceptions.HDWalletException
import io.reactivex.Maybe

internal class PayloadDataManagerSeedAccessAdapter(
    private val payloadDataManager: PayloadDataManager
) : SeedAccess {

    override val seed: Maybe<Seed>
        get() {
            try {
                val hdWallet = payloadDataManager.wallet?.hdWallets?.get(0)
                val hdSeed = hdWallet?.hdSeed
                val masterKey = hdWallet?.masterKey?.privKeyBytes
                return if (hdSeed == null || masterKey == null) {
                    Maybe.empty()
                } else {
                    Maybe.just(
                        Seed(
                            hdSeed = hdSeed,
                            masterKey = masterKey
                        )
                    )
                }
            } catch (hd: HDWalletException) {
                return Maybe.empty()
            }
        }
}
