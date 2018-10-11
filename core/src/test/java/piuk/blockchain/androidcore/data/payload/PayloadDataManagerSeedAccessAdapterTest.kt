package piuk.blockchain.androidcore.data.payload

import com.blockchain.wallet.NoSeedException
import com.blockchain.wallet.SeedAccess
import com.nhaarman.mockito_kotlin.mock
import info.blockchain.wallet.payload.data.HDWallet
import info.blockchain.wallet.payload.data.Wallet
import org.amshove.kluent.`it returns`
import org.amshove.kluent.`should be`
import org.amshove.kluent.`should throw`
import org.junit.Test

class PayloadDataManagerSeedAccessAdapterTest {

    @Test
    fun `extracts seed from the first HDWallet`() {
        val theSeed = byteArrayOf(1, 2, 3)
        val hdWallet = mock<HDWallet> {
            on { hdSeed } `it returns` theSeed
        }
        val wallet = mock<Wallet> {
            on { hdWallets } `it returns` listOf(hdWallet)
        }
        val payloadDataManager = mock<PayloadDataManager> {
            on { this.wallet } `it returns` wallet
        }
        val seedAccess: SeedAccess = PayloadDataManagerSeedAccessAdapter(payloadDataManager)
        seedAccess.hdSeed `should be` theSeed
    }

    @Test
    fun `if the list is null, throws`() {
        val wallet = mock<Wallet> {
            on { hdWallets } `it returns` null
        }
        val payloadDataManager = mock<PayloadDataManager> {
            on { this.wallet } `it returns` wallet
        }
        val seedAccess: SeedAccess = PayloadDataManagerSeedAccessAdapter(payloadDataManager);
        {
            seedAccess.hdSeed
        } `should throw` NoSeedException::class
    }

    @Test
    fun `if the wallet is null, throws`() {
        val payloadDataManager = mock<PayloadDataManager> {
            on { this.wallet } `it returns` null
        }
        val seedAccess: SeedAccess = PayloadDataManagerSeedAccessAdapter(payloadDataManager);
        {
            seedAccess.hdSeed
        } `should throw` NoSeedException::class
    }
}
