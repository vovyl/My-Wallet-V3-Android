package com.blockchain.sunriver.datamanager

import com.blockchain.metadata.MetadataRepository
import com.blockchain.metadata.MetadataWarningLog
import com.blockchain.sunriver.derivation.deriveXlmAccountKeyPair
import com.blockchain.wallet.DefaultLabels
import com.blockchain.wallet.NoSeedException
import com.blockchain.wallet.SeedAccessWithoutPrompt
import info.blockchain.balance.CryptoCurrency
import io.reactivex.Maybe
import io.reactivex.Single

internal class XlmMetaDataInitializer(
    private val defaultLabels: DefaultLabels,
    private val repository: MetadataRepository,
    private val seedAccess: SeedAccessWithoutPrompt,
    private val metadataWarningLog: MetadataWarningLog
) {

    // TODO("AND-1611") remove usages
    @Deprecated("Better to use the maybe version and handle empty case")
    internal fun initWallet(): Single<XlmMetaData> =
        Maybe.concat(
            initWalletMaybe(),
            Maybe.error(NoSeedException())
        ).firstOrError()

    internal fun initWalletMaybe(): Maybe<XlmMetaData> =
        Maybe.concat(
            load(),
            createAndSave()
            // TODO("AND-1611") createAndSaveUsingSecondPassword
        ).firstElement()

    private fun load(): Maybe<XlmMetaData> =
        repository.loadMetadata(XlmMetaData.MetaDataType, XlmMetaData::class.java)
            .ignoreBadMetadata()
            .compareForLog()

    private fun Maybe<XlmMetaData>.compareForLog(): Maybe<XlmMetaData> =
        flatMap { loaded ->
            Maybe.concat(
                newXlmMetaData(defaultLabels[CryptoCurrency.XLM])
                    .doOnSuccess { expected ->
                        inspectLoadedData(loaded, expected)
                    }
                    .map { loaded },
                this
            ).firstElement()
        }

    private fun createAndSave(): Maybe<XlmMetaData> =
        newXlmMetaData(defaultLabels[CryptoCurrency.XLM])
            .flatMap { newData ->
                repository.saveMetadata(
                    newData,
                    XlmMetaData::class.java,
                    XlmMetaData.MetaDataType
                ).andThen(Maybe.just(newData))
            }

    /**
     * Logs any discrepancies between the expected first account, and the loaded first account.
     * If it cannot test for discrepancies (e.g., no seed available at the time) it does not log anything.
     */
    private fun inspectLoadedData(loaded: XlmMetaData, expected: XlmMetaData) {
        val expectedAccount = expected.accounts?.get(0)
        val loadedAccount = loaded.accounts?.get(0)
        if (expectedAccount?.secret != loadedAccount?.secret ||
            expectedAccount?.publicKey != loadedAccount?.publicKey
        ) {
            metadataWarningLog.logWarning("Xlm metadata expected did not match that loaded")
        }
    }

    private fun newXlmMetaData(defaultLabel: String): Maybe<XlmMetaData> =
        seedAccess.seed.map { seed ->
            val derived = deriveXlmAccountKeyPair(seed.hdSeed, 0)
            XlmMetaData(
                defaultAccountIndex = 0,
                accounts = listOf(
                    XlmAccount(
                        publicKey = derived.accountId,
                        secret = String(derived.secret),
                        label = defaultLabel,
                        archived = false
                    )
                ),
                transactionNotes = emptyMap()
            )
        }
}

private fun Maybe<XlmMetaData>.ignoreBadMetadata(): Maybe<XlmMetaData> =
    filter { !(it.accounts?.isEmpty() ?: true) }
