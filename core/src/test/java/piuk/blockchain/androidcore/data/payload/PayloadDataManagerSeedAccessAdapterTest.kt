package piuk.blockchain.androidcore.data.payload

import com.blockchain.wallet.SeedAccessWithoutPrompt
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import info.blockchain.wallet.exceptions.HDWalletException
import info.blockchain.wallet.payload.data.HDWallet
import info.blockchain.wallet.payload.data.Wallet
import org.amshove.kluent.`it returns`
import org.amshove.kluent.`should be`
import org.bitcoinj.crypto.DeterministicKey
import org.junit.Test

class PayloadDataManagerSeedAccessAdapterTest {

    @Test
    fun `extracts seed from the first HDWallet`() {
        val theSeed = byteArrayOf(1, 2, 3)
        val theMasterKeyBytes = byteArrayOf(4, 5, 6)
        val seedAccess: SeedAccessWithoutPrompt = PayloadDataManagerSeedAccessAdapter(
            givenADecodedPayload(theMasterKeyBytes, theSeed)
        )
        seedAccess.seed.test().values().single()
            .apply {
                hdSeed `should be` theSeed
                masterKey `should be` theMasterKeyBytes
            }
    }

    @Test
    fun `if the HD wallet throws HD Exception, returns empty`() {
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
        val seedAccess: SeedAccessWithoutPrompt = PayloadDataManagerSeedAccessAdapter(payloadDataManager)
        seedAccess.seed
            .test()
            .assertComplete()
            .assertValueCount(0)
    }

    @Test
    fun `if the list is null, returns empty`() {
        val wallet = mock<Wallet> {
            on { hdWallets } `it returns` null
        }
        val payloadDataManager = mock<PayloadDataManager> {
            on { this.wallet } `it returns` wallet
        }
        val seedAccess: SeedAccessWithoutPrompt = PayloadDataManagerSeedAccessAdapter(payloadDataManager)
        seedAccess.seed
            .test()
            .assertComplete()
            .assertValueCount(0)
    }

    @Test
    fun `if the wallet is null, returns empty`() {
        val payloadDataManager = mock<PayloadDataManager> {
            on { this.wallet } `it returns` null
        }
        val seedAccess: SeedAccessWithoutPrompt = PayloadDataManagerSeedAccessAdapter(payloadDataManager)
        seedAccess.seed
            .test()
            .assertComplete()
            .assertValueCount(0)
    }

    @Test
    fun `extracts seed from the first HDWallet without decrypting - when already decoded`() {
        val theSeed = byteArrayOf(1, 2, 3)
        val theMasterKeyBytes = byteArrayOf(4, 5, 6)
        val payloadDataManager = givenADecodedPayload(theMasterKeyBytes, theSeed)
        val seedAccess: SeedAccessWithoutPrompt = PayloadDataManagerSeedAccessAdapter(
            payloadDataManager
        )
        seedAccess.seed("PASSWORD").test().values().single()
            .apply {
                hdSeed `should be` theSeed
                masterKey `should be` theMasterKeyBytes
            }
        verify(payloadDataManager, never()).decryptHDWallet(any())
    }

    @Test
    fun `decrypts if required`() {
        val payloadDataManager = mock<PayloadDataManager> {
            on { this.wallet } `it returns` null
        }
        val seedAccess: SeedAccessWithoutPrompt = PayloadDataManagerSeedAccessAdapter(
            payloadDataManager
        )
        seedAccess.seed("PASSWORD").test()
        verify(payloadDataManager).decryptHDWallet("PASSWORD")
    }
}

private fun givenADecodedPayload(
    theMasterKeyBytes: ByteArray,
    theSeed: ByteArray
): PayloadDataManager {
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
    return mock {
        on { this.wallet } `it returns` wallet
    }
}
