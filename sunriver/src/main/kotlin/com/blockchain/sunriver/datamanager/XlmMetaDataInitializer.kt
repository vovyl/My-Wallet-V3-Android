package com.blockchain.sunriver.datamanager

import com.blockchain.metadata.MetadataRepository
import com.blockchain.metadata.MetadataWarningLog
import com.blockchain.sunriver.derivation.deriveXlmAccountKeyPair
import com.blockchain.wallet.DefaultLabels
import com.blockchain.wallet.SeedAccess
import info.blockchain.balance.CryptoCurrency
import io.reactivex.Maybe
import io.reactivex.Single

internal class XlmMetaDataInitializer(
    private val defaultLabels: DefaultLabels,
    private val repository: MetadataRepository,
    private val seedAccess: SeedAccess,
    private val metadataWarningLog: MetadataWarningLog
) {

    internal fun initWallet(): Single<XlmMetaData> {
        val metadata = repository.loadMetadata(XlmMetaData.MetaDataType, XlmMetaData::class.java)
        return metadata
            .ignoreBadMetadata()
            .isEmpty
            .flatMap { empty ->
                if (empty) {
                    val newData = newXlmMetaData(defaultLabels[CryptoCurrency.XLM])
                    repository.saveMetadata(
                        newData,
                        XlmMetaData::class.java,
                        XlmMetaData.MetaDataType
                    ).toSingle<XlmMetaData> { newData }
                } else {
                    metadata.toSingle()
                        .doOnSuccess { loaded ->
                            tryInspectLoadedData(loaded)
                        }
                }
            }
    }

    /**
     * Logs any discrepancies between the expected first account, and the loaded first account.
     * If it cannot test for discrepancies (e.g., no seed available at the time) it does not log anything.
     */
    private fun tryInspectLoadedData(loaded: XlmMetaData) {
        try {
            inspectLoadedData(loaded)
        } catch (e: Exception) {
        }
    }

    private fun inspectLoadedData(loaded: XlmMetaData) {
        val expectedData = newXlmMetaData("")
        val expectedAccount = expectedData.accounts?.get(0)
        val loadedAccount = loaded.accounts?.get(0)
        if (expectedAccount?.secret != loadedAccount?.secret ||
            expectedAccount?.publicKey != loadedAccount?.publicKey
        ) {
            metadataWarningLog.logWarning("Xlm metadata expected did not match that loaded")
        }
    }

    private fun newXlmMetaData(defaultLabel: String): XlmMetaData {
        val derived = deriveXlmAccountKeyPair(seedAccess.hdSeed, 0)
        return XlmMetaData(
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

private fun Maybe<XlmMetaData>.ignoreBadMetadata() = filter { !(it.accounts?.isEmpty() ?: true) }
