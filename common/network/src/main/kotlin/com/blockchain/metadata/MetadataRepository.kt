package com.blockchain.metadata

import com.blockchain.serialization.JsonSerializable
import io.reactivex.Completable
import io.reactivex.Maybe

interface MetadataRepository {

    fun <T : JsonSerializable> loadMetadata(metadataType: Int, clazz: Class<T>): Maybe<T>

    fun <T : JsonSerializable> saveMetadata(data: T, clazz: Class<T>, metadataType: Int): Completable
}
