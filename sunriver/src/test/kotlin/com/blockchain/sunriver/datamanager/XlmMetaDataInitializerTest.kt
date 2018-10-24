package com.blockchain.sunriver.datamanager

import com.blockchain.metadata.MetadataRepository
import com.blockchain.metadata.MetadataWarningLog
import com.blockchain.serialization.fromMoshiJson
import com.blockchain.wallet.DefaultLabels
import com.blockchain.wallet.NoSeedException
import com.blockchain.wallet.Seed
import com.blockchain.wallet.SeedAccessWithoutPrompt
import com.nhaarman.mockito_kotlin.KStubbing
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import info.blockchain.balance.CryptoCurrency
import io.github.novacrypto.bip39.SeedCalculator
import io.reactivex.Completable
import io.reactivex.Maybe
import org.amshove.kluent.`it returns`
import org.amshove.kluent.`should equal`
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
            givenDefaultXlmLabel("My Lumen Wallet"),
            repository,
            givenSeedFor(mnemonic = "illness spike retreat truth genius clock brain pass fit cave bargain toe"),
            log
        )
            .initWallet()
            .test()
            .assertNoErrors()
            .assertComplete()
            .values() `should equal` listOf(expectedData)

        repository.assertSaved(expectedData)
        repository.assertLoaded()
        assertNoWarnings()
        assertSingleMetaDataLoad(repository)
    }

    @Test
    fun `if the meta data is missing, it will create it - alternative values`() {
        val expectedData = XlmMetaData(
            defaultAccountIndex = 0,
            accounts = listOf(
                XlmAccount(
                    publicKey = "GAVXVW5MCK7Q66RIBWZZKZEDQTRXWCZUP4DIIFXCCENGW2P6W4OA34RH",
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
            givenDefaultXlmLabel("The Lumen Wallet"),
            repository,
            givenSeedFor(
                mnemonic = "resource asthma orphan phone ice canvas " +
                    "fire useful arch jewel impose vague theory cushion top"
            ),
            log
        )
            .initWallet()
            .test()
            .assertNoErrors()
            .assertComplete()
            .values() `should equal` listOf(expectedData)

        repository.assertSaved(expectedData)
        repository.assertLoaded()
        assertNoWarnings()
        assertSingleMetaDataLoad(repository)
    }

    @Test
    fun `if the meta data is there, it will not create it`() {
        val expectedData = XlmMetaData(
            defaultAccountIndex = 0,
            accounts = listOf(
                XlmAccount(
                    publicKey = "GDRXE2BQUC3AZNPVFSCEZ76NJ3WWL25FYFK6RGZGIEKWE4SOOHSUJUJ6",
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
            givenDefaultXlmLabel("My Lumen Wallet"),
            repository,
            givenSeedFor(mnemonic = "illness spike retreat truth genius clock brain pass fit cave bargain toe"),
            log
        )
            .initWallet()
            .test()
            .assertNoErrors()
            .assertComplete()
            .values() `should equal` listOf(expectedData)

        repository.assertNothingSaved()
        repository.assertLoaded()
        assertNoWarnings()
        assertSingleMetaDataLoad(repository)
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
            givenDefaultXlmLabel("My Lumen Wallet"),
            repository,
            givenSeedFor(mnemonic = "illness spike retreat truth genius clock brain pass fit cave bargain toe"),
            log
        )
            .initWallet()
            .test()
            .assertFailureAndMessage(Exception::class.java, "Save fail")
        assertNoWarnings()
        assertSingleMetaDataLoad(repository)
    }

    @Test
    fun `if the seed is not present when it needs to create it, throw an exception`() {
        val repository = mock<MetadataRepository> {
            successfulSave()
            emptyLoad()
        }
        XlmMetaDataInitializer(
            givenDefaultXlmLabel("My Lumen Wallet"),
            repository,
            givenNoSeed(),
            log
        )
            .initWallet()
            .test()
            .assertError(NoSeedException::class.java)

        repository.assertNothingSaved()
        repository.assertLoaded()
        assertNoWarnings()
        assertSingleMetaDataLoad(repository)
    }

    @Test
    fun `if the seed is not present when it needs to create it, return empty`() {
        val repository = mock<MetadataRepository> {
            successfulSave()
            emptyLoad()
        }
        XlmMetaDataInitializer(
            givenDefaultXlmLabel("My Lumen Wallet"),
            repository,
            givenNoSeed(),
            log
        )
            .initWalletMaybe()
            .test()
            .assertComplete()
            .assertValueCount(0)

        repository.assertNothingSaved()
        repository.assertLoaded()
        assertNoWarnings()
        assertSingleMetaDataLoad(repository)
    }

    @Test
    fun `if the seed is not present, but it doesn't need it, then there is no error`() {
        val expectedData = XlmMetaData(
            defaultAccountIndex = 0,
            accounts = listOf(
                XlmAccount(
                    publicKey = "GDRXE2BQUC3AZNPVFSCEZ76NJ3WWL25FYFK6RGZGIEKWE4SOOHSUJUJ6",
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
            givenDefaultXlmLabel("My Lumen Wallet"),
            repository,
            givenNoSeed(),
            log
        )
            .initWallet()
            .test()
            .assertNoErrors()
            .assertComplete()
            .values() `should equal` listOf(expectedData)

        repository.assertNothingSaved()
        repository.assertLoaded()
        assertNoWarnings()
        assertSingleMetaDataLoad(repository)
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
                    label = "My Lumen Wallet",
                    archived = false
                )
            ),
            transactionNotes = emptyMap()
        )
        XlmMetaDataInitializer(
            givenDefaultXlmLabel("My Lumen Wallet"),
            repository,
            givenSeedFor(mnemonic = "illness spike retreat truth genius clock brain pass fit cave bargain toe"),
            log
        )
            .initWallet()
            .test()
            .assertNoErrors()
            .assertComplete()
            .values() `should equal` listOf(expectedData)

        repository.assertSaved(expectedData)
        repository.assertLoaded()
        assertNoWarnings()
        assertSingleMetaDataLoad(repository)
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
                    label = "My Lumen Wallet",
                    archived = false
                )
            ),
            transactionNotes = emptyMap()
        )
        XlmMetaDataInitializer(
            givenDefaultXlmLabel("My Lumen Wallet"),
            repository,
            givenSeedFor(mnemonic = "illness spike retreat truth genius clock brain pass fit cave bargain toe"),
            log
        )
            .initWallet()
            .test()
            .assertNoErrors()
            .assertComplete()
            .values() `should equal` listOf(expectedData)

        repository.assertSaved(expectedData)
        repository.assertLoaded()
        assertNoWarnings()
        assertSingleMetaDataLoad(repository)
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
                    label = "My Lumen Wallet X",
                    archived = false
                )
            ),
            transactionNotes = emptyMap()
        )
        XlmMetaDataInitializer(
            givenDefaultXlmLabel("My Lumen Wallet X"),
            repository,
            givenSeedFor(
                mnemonic = "bench hurt jump file august wise shallow faculty impulse spring exact slush " +
                    "thunder author capable act festival slice deposit sauce coconut afford frown better"
            ),
            log
        )
            .initWallet()
            .test()
            .assertNoErrors()
            .assertComplete()
            .values() `should equal` listOf(expectedData)

        repository.assertSaved(expectedData)
        repository.assertLoaded()
        assertNoWarnings()
        assertSingleMetaDataLoad(repository)
    }

    @Test
    fun `if the meta data is there, but the first account does not match the expected values, log warning`() {
        val expectedData = XlmMetaData(
            defaultAccountIndex = 0,
            accounts = listOf(
                XlmAccount(
                    publicKey = "GDRXE2BQUC3AZNPVFSCEZ76NJ3WWL25FYFK6RGZGIEKWE4SOOHSUJUJ6",
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
            givenDefaultXlmLabel("My Lumen Wallet"),
            repository,
            givenSeedFor(
                mnemonic = "cable spray genius state float twenty onion head street palace net private " +
                    "method loan turn phrase state blanket interest dry amazing dress blast tube"
            ),
            log
        )
            .initWallet()
            .test()
            .assertNoErrors()
            .assertComplete()
            .values() `should equal` listOf(expectedData)

        repository.assertNothingSaved()
        repository.assertLoaded()
        verify(log).logWarning("Xlm metadata expected did not match that loaded")
        assertSingleMetaDataLoad(repository)
    }

    private fun assertSingleMetaDataLoad(repository: MetadataRepository) {
        verify(repository).loadMetadata(any(), eq(XlmMetaData::class.java))
    }

    private fun assertNoWarnings() {
        verify(log, never()).logWarning(any())
    }
}

private fun givenSeedFor(mnemonic: String): SeedAccessWithoutPrompt =
    object : SeedAccessWithoutPrompt {
        override val seed: Maybe<Seed>
            get() = Maybe.just(
                Seed(
                    hdSeed = SeedCalculator().calculateSeed(mnemonic, ""),
                    masterKey = ByteArray(0)
                )
            )

        override fun seed(validatedSecondPassword: String): Maybe<Seed> {
            throw Exception("Unexpected")
        }
    }

private fun givenNoSeed(): SeedAccessWithoutPrompt =
    object : SeedAccessWithoutPrompt {
        override val seed: Maybe<Seed>
            get() = Maybe.empty()

        override fun seed(validatedSecondPassword: String): Maybe<Seed> {
            throw Exception("Unexpected")
        }
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

private fun givenDefaultXlmLabel(defaultLabel: String): DefaultLabels =
    mock {
        on { get(CryptoCurrency.XLM) } `it returns` defaultLabel
    }
