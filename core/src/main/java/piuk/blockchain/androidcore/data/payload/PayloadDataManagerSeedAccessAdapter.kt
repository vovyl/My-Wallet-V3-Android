package piuk.blockchain.androidcore.data.payload

import com.blockchain.wallet.NoSeedException
import com.blockchain.wallet.SeedAccess

internal class PayloadDataManagerSeedAccessAdapter(
    private val payloadDataManager: PayloadDataManager
) : SeedAccess {

    override val hdSeed: ByteArray
        get() = payloadDataManager.wallet?.hdWallets?.get(0)?.hdSeed ?: throw NoSeedException()
}
