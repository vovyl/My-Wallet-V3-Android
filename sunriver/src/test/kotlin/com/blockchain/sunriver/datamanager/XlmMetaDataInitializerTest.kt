package com.blockchain.sunriver.datamanager

import com.blockchain.metadata.MetadataRepository
import com.blockchain.metadata.MetadataWarningLog
import com.blockchain.serialization.fromMoshiJson
import com.nhaarman.mockito_kotlin.KStubbing
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import io.reactivex.Completable
import io.reactivex.Maybe
import org.amshove.kluent.`it returns`
import org.amshove.kluent.`should be instance of`
import org.amshove.kluent.`should equal`
import org.bitcoinj.crypto.MnemonicCode
import org.junit.Test

class XlmMetaDataInitializerTest {

    private val log: MetadataWarningLog = mock()

    @Test
    fun `if the meta data is missing, it will create it`() {
        val expectedData = XlmMetaData(
            defaultAccountIndex = 0,
            accounts = listOf(
                XlmAccount(
                    publicKey = "GDRXE2BQUC3AZNPVFSCEZ76NJ3WWL25FYFK6RGZGIEKWE4SOOHSUJUJ6",
                    secret = "SBGWSG6BTNCKCOB3DIFBGCVMUPQFYPA2G4O34RMTB343OYPXU5DJDVMN",
                    label = "My Lumen Wallet",
                    archived = false
                )
            ),
            transactionNotes = emptyMap()
        )
        val repository = mock<MetadataRepository> {
            successfulSave()
            emptyLoad()
        }
        XlmMetaDataInitializer(
            repository,
            givenSeedFor(mnemonic = "illness spike retreat truth genius clock brain pass fit cave bargain toe"),
            log
        )
            .initWallet("My Lumen Wallet")
            .test()
            .values() `should equal` listOf(expectedData)

        repository.assertSaved(expectedData)
        repository.assertLoaded()
        assertNoWarnings()
    }

    @Test
    fun `if the meta data is missing, it will create it - alternative values`() {
        val expectedData = XlmMetaData(
            defaultAccountIndex = 0,
            accounts = listOf(
                XlmAccount(
                    publicKey = "GAVXVW5MCK7Q66RIBWZZKZEDQTRXWCZUP4DIIFXCCENGW2P6W4OA34RH",
                    secret = "SAKS7I2PNDBE5SJSUSU2XLJ7K5XJ3V3K4UDFAHMSBQYPOKE247VHAGDB",
                    label = "The Lumen Wallet",
                    archived = false
                )
            ),
            transactionNotes = emptyMap()
        )
        val repository = mock<MetadataRepository> {
            successfulSave()
            emptyLoad()
        }
        XlmMetaDataInitializer(
            repository,
            givenSeedFor(
                mnemonic = "resource asthma orphan phone ice canvas " +
                    "fire useful arch jewel impose vague theory cushion top"
            ),
            log
        )
            .initWallet("The Lumen Wallet")
            .test()
            .values() `should equal` listOf(expectedData)

        repository.assertSaved(expectedData)
        repository.assertLoaded()
        assertNoWarnings()
    }

    @Test
    fun `if the meta data is there, it will not create it`() {
        val expectedData = XlmMetaData(
            defaultAccountIndex = 0,
            accounts = listOf(
                XlmAccount(
                    publicKey = "GDRXE2BQUC3AZNPVFSCEZ76NJ3WWL25FYFK6RGZGIEKWE4SOOHSUJUJ6",
                    secret = "SBGWSG6BTNCKCOB3DIFBGCVMUPQFYPA2G4O34RMTB343OYPXU5DJDVMN",
                    label = "My Lumen Wallet",
                    archived = false
                )
            ),
            transactionNotes = emptyMap()
        )
        val repository = mock<MetadataRepository> {
            successfulSave()
            loads(expectedData)
        }
        XlmMetaDataInitializer(
            repository,
            givenSeedFor(mnemonic = "illness spike retreat truth genius clock brain pass fit cave bargain toe"),
            log
        )
            .initWallet("My Lumen Wallet")
            .test()
            .values() `should equal` listOf(expectedData)

        repository.assertNothingSaved()
        repository.assertLoaded()
        assertNoWarnings()
    }

    @Test
    fun `if the save fails, the error bubbles up`() {
        val repository = mock<MetadataRepository> {
            on {
                saveMetadata<XlmMetaData>(
                    any(),
                    any(),
                    eq(XlmMetaData.MetaDataType)
                )
            } `it returns` Completable.error(Exception("Save fail"))
            emptyLoad()
        }
        XlmMetaDataInitializer(
            repository,
            givenSeedFor(mnemonic = "illness spike retreat truth genius clock brain pass fit cave bargain toe"),
            log
        )
            .initWallet("My Lumen Wallet")
            .test()
            .assertError {
                it `should be instance of` Exception::class.java
                it.message `should equal` "Save fail"
                true
            }
        assertNoWarnings()
    }

    @Test
    fun `if the seed is not present when it needs to create it, throw an exception`() {
        val repository = mock<MetadataRepository> {
            successfulSave()
            emptyLoad()
        }
        XlmMetaDataInitializer(
            repository,
            givenNoSeed(),
            log
        )
            .initWallet("My Lumen Wallet")
            .test()
            .assertError {
                it `should be instance of` Exception::class.java
                it.message `should equal` "No seed is available"
                true
            }

        repository.assertNothingSaved()
        repository.assertLoaded()
        assertNoWarnings()
    }

    @Test
    fun `if the seed is not present, but it doesn't need it, then there is no error`() {
        val expectedData = XlmMetaData(
            defaultAccountIndex = 0,
            accounts = listOf(
                XlmAccount(
                    publicKey = "GDRXE2BQUC3AZNPVFSCEZ76NJ3WWL25FYFK6RGZGIEKWE4SOOHSUJUJ6",
                    secret = "SBGWSG6BTNCKCOB3DIFBGCVMUPQFYPA2G4O34RMTB343OYPXU5DJDVMN",
                    label = "My Lumen Wallet",
                    archived = false
                )
            ),
            transactionNotes = emptyMap()
        )
        val repository = mock<MetadataRepository> {
            successfulSave()
            loads(expectedData)
        }
        XlmMetaDataInitializer(
            repository,
            givenNoSeed(),
            log
        )
            .initWallet("My Lumen Wallet")
            .test()
            .values() `should equal` listOf(expectedData)

        repository.assertNothingSaved()
        repository.assertLoaded()
        assertNoWarnings()
    }

    @Test
    fun `if the meta data has no accounts, recreate it`() {
        val badData = XlmMetaData(
            defaultAccountIndex = 0,
            accounts = emptyList(),
            transactionNotes = emptyMap()
        )
        val repository = mock<MetadataRepository> {
            successfulSave()
            loads(badData)
        }
        val expectedData = XlmMetaData(
            defaultAccountIndex = 0,
            accounts = listOf(
                XlmAccount(
                    publicKey = "GDRXE2BQUC3AZNPVFSCEZ76NJ3WWL25FYFK6RGZGIEKWE4SOOHSUJUJ6",
                    secret = "SBGWSG6BTNCKCOB3DIFBGCVMUPQFYPA2G4O34RMTB343OYPXU5DJDVMN",
                    label = "My Lumen Wallet",
                    archived = false
                )
            ),
            transactionNotes = emptyMap()
        )
        XlmMetaDataInitializer(
            repository,
            givenSeedFor(mnemonic = "illness spike retreat truth genius clock brain pass fit cave bargain toe"),
            log
        )
            .initWallet("My Lumen Wallet")
            .test()
            .values() `should equal` listOf(expectedData)

        repository.assertSaved(expectedData)
        repository.assertLoaded()
        assertNoWarnings()
    }

    @Test
    fun `if the meta data account is null, recreate it`() {
        val badData = XlmMetaData(
            defaultAccountIndex = 0,
            accounts = null,
            transactionNotes = emptyMap()
        )
        val repository = mock<MetadataRepository> {
            successfulSave()
            loads(badData)
        }
        val expectedData = XlmMetaData(
            defaultAccountIndex = 0,
            accounts = listOf(
                XlmAccount(
                    publicKey = "GDRXE2BQUC3AZNPVFSCEZ76NJ3WWL25FYFK6RGZGIEKWE4SOOHSUJUJ6",
                    secret = "SBGWSG6BTNCKCOB3DIFBGCVMUPQFYPA2G4O34RMTB343OYPXU5DJDVMN",
                    label = "My Lumen Wallet",
                    archived = false
                )
            ),
            transactionNotes = emptyMap()
        )
        XlmMetaDataInitializer(
            repository,
            givenSeedFor(mnemonic = "illness spike retreat truth genius clock brain pass fit cave bargain toe"),
            log
        )
            .initWallet("My Lumen Wallet")
            .test()
            .values() `should equal` listOf(expectedData)

        repository.assertSaved(expectedData)
        repository.assertLoaded()
        assertNoWarnings()
    }

    @Test
    fun `if the meta data account is empty object, recreate it`() {
        val badData = XlmMetaData::class.fromMoshiJson("{}")
        val repository = mock<MetadataRepository> {
            successfulSave()
            loads(badData)
        }
        val expectedData = XlmMetaData(
            defaultAccountIndex = 0,
            accounts = listOf(
                XlmAccount(
                    publicKey = "GC3MMSXBWHL6CPOAVERSJITX7BH76YU252WGLUOM5CJX3E7UCYZBTPJQ",
                    secret = "SAEWIVK3VLNEJ3WEJRZXQGDAS5NVG2BYSYDFRSH4GKVTS5RXNVED5AX7",
                    label = "My Lumen Wallet X",
                    archived = false
                )
            ),
            transactionNotes = emptyMap()
        )
        XlmMetaDataInitializer(
            repository,
            givenSeedFor(
                mnemonic = "bench hurt jump file august wise shallow faculty impulse spring exact slush " +
                    "thunder author capable act festival slice deposit sauce coconut afford frown better"
            ),
            log
        )
            .initWallet("My Lumen Wallet X")
            .test()
            .values() `should equal` listOf(expectedData)

        repository.assertSaved(expectedData)
        repository.assertLoaded()
        assertNoWarnings()
    }

    @Test
    fun `if the meta data is there, but the first account does not match the expected values, log warning`() {
        val expectedData = XlmMetaData(
            defaultAccountIndex = 0,
            accounts = listOf(
                XlmAccount(
                    publicKey = "GDRXE2BQUC3AZNPVFSCEZ76NJ3WWL25FYFK6RGZGIEKWE4SOOHSUJUJ6",
                    secret = "SBGWSG6BTNCKCOB3DIFBGCVMUPQFYPA2G4O34RMTB343OYPXU5DJDVMN",
                    label = "My Lumen Wallet",
                    archived = false
                )
            ),
            transactionNotes = emptyMap()
        )
        val repository = mock<MetadataRepository> {
            successfulSave()
            loads(expectedData)
        }
        XlmMetaDataInitializer(
            repository,
            givenSeedFor(
                mnemonic = "cable spray genius state float twenty onion head street palace net private " +
                    "method loan turn phrase state blanket interest dry amazing dress blast tube"
            ),
            log
        )
            .initWallet("My Lumen Wallet")
            .test()
            .values() `should equal` listOf(expectedData)

        repository.assertNothingSaved()
        repository.assertLoaded()
        verify(log).logWarning("Xlm metadata expected did not match that loaded")
    }

    private fun assertNoWarnings() {
        verify(log, never()).logWarning(any())
    }
}

private fun givenSeedFor(mnemonic: String): SeedAccess =
    object : SeedAccess {
        override val hdSeed: ByteArray
            get() = MnemonicCode.toSeed(mnemonic.split(" "), "")
    }

private fun givenNoSeed(): SeedAccess =
    object : SeedAccess {
        override val hdSeed: ByteArray get() = throw Exception("No seed is available")
    }

private fun MetadataRepository.assertNothingSaved() {
    verify(this, never()).saveMetadata<XlmMetaData>(any(), any(), any())
}

private fun MetadataRepository.assertLoaded() {
    verify(this).loadMetadata(XlmMetaData.MetaDataType, XlmMetaData::class.java)
}

private fun MetadataRepository.assertSaved(
    value: XlmMetaData
) {
    verify(this).saveMetadata(
        eq(
            value
        ),
        eq(XlmMetaData::class.java),
        eq(XlmMetaData.MetaDataType)
    )
}

private fun KStubbing<MetadataRepository>.emptyLoad() {
    on { loadMetadata(XlmMetaData.MetaDataType, XlmMetaData::class.java) } `it returns` Maybe.empty()
}

private fun KStubbing<MetadataRepository>.loads(expectedData: XlmMetaData) {
    on { loadMetadata(XlmMetaData.MetaDataType, XlmMetaData::class.java) } `it returns` Maybe.just(
        expectedData
    )
}

private fun KStubbing<MetadataRepository>.successfulSave() {
    on {
        saveMetadata<XlmMetaData>(
            any(),
            any(),
            eq(XlmMetaData.MetaDataType)
        )
    } `it returns` Completable.complete()
}
