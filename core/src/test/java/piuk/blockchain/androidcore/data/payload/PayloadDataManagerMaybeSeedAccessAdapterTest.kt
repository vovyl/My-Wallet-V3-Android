package piuk.blockchain.androidcore.data.payload

import com.blockchain.wallet.SeedAccess
import com.nhaarman.mockito_kotlin.mock
import info.blockchain.wallet.exceptions.HDWalletException
import info.blockchain.wallet.payload.data.HDWallet
import info.blockchain.wallet.payload.data.Wallet
import org.amshove.kluent.`it returns`
import org.amshove.kluent.`should be`
import org.bitcoinj.crypto.DeterministicKey
import org.junit.Test

class PayloadDataManagerMaybeSeedAccessAdapterTest {

    @Test
    fun `extracts seed from the first HDWallet`() {
        val theSeed = byteArrayOf(1, 2, 3)
        val theMasterKeyBytes = byteArrayOf(4, 5, 6)
        val theMasterKey: DeterministicKey = mock {
            on { privKeyBytes } `it returns` theMasterKeyBytes
        }
        val hdWallet = mock<HDWallet> {
            on { hdSeed } `it returns` theSeed
            on { masterKey } `it returns` theMasterKey
        }
        val wallet = mock<Wallet> {
            on { hdWallets } `it returns` listOf(hdWallet)
        }
        val payloadDataManager = mock<PayloadDataManager> {
            on { this.wallet } `it returns` wallet
        }
        val seedAccess: SeedAccess = PayloadDataManagerSeedAccessAdapter(payloadDataManager)
        seedAccess.seed.test().values().single()
            .apply {
                hdSeed `should be` theSeed
                masterKey `should be` theMasterKeyBytes
            }
    }

    @Test
    fun `if the HD wallet throws HD Exception, it's empty`() {
        val theSeed = byteArrayOf(1, 2, 3)
        val hdWallet = mock<HDWallet> {
            on { hdSeed } `it returns` theSeed
            on { masterKey }.thenThrow(HDWalletException())
        }
        val wallet = mock<Wallet> {
            on { hdWallets } `it returns` listOf(hdWallet)
        }
        val payloadDataManager = mock<PayloadDataManager> {
            on { this.wallet } `it returns` wallet
        }
        val seedAccess: SeedAccess = PayloadDataManagerSeedAccessAdapter(payloadDataManager)
        seedAccess.seed
            .test()
            .assertComplete()
            .assertValueCount(0)
    }

    @Test
    fun `if the list is null, throws`() {
        val wallet = mock<Wallet> {
            on { hdWallets } `it returns` null
        }
        val payloadDataManager = mock<PayloadDataManager> {
            on { this.wallet } `it returns` wallet
        }
        val seedAccess: SeedAccess = PayloadDataManagerSeedAccessAdapter(payloadDataManager)
        seedAccess.seed
            .test()
            .assertComplete()
            .assertValueCount(0)
    }

    @Test
    fun `if the wallet is null, throws`() {
        val payloadDataManager = mock<PayloadDataManager> {
            on { this.wallet } `it returns` null
        }
        val seedAccess: SeedAccess = PayloadDataManagerSeedAccessAdapter(payloadDataManager)
        seedAccess.seed
            .test()
            .assertComplete()
            .assertValueCount(0)
    }
}
