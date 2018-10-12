package com.blockchain.lockbox.data

import com.blockchain.exceptions.MetadataNotFoundException
import com.blockchain.lockbox.data.models.LockboxMetadata
import com.blockchain.serialization.fromMoshiJson
import com.google.common.base.Optional
import io.reactivex.Single
import piuk.blockchain.androidcore.data.metadata.MetadataManager

class LockboxDataManager(
    private val metadataManager: MetadataManager,
    private val isAvailable: Boolean
) {

    fun isLockboxAvailable(): Boolean = isAvailable

    fun hasLockbox(): Single<Boolean> = fetchLockbox()
        .map { it.hasLockbox() }
        .cache()
        .onErrorReturn { false }

    private fun fetchLockbox(): Single<LockboxMetadata> = metadataManager.fetchMetadata(LockboxMetadata.MetaDataType)
        .map { it.parseLockboxOrThrow() }
        .singleOrError()

    private fun Optional<String>.parseLockboxOrThrow(): LockboxMetadata {
        if (isPresent) {
            return LockboxMetadata::class.fromMoshiJson(get())
        } else {
            throw MetadataNotFoundException("Lockbox metadata is empty")
        }
    }
}