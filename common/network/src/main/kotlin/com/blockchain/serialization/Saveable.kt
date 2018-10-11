package com.blockchain.serialization

/**
 * Exposes a meta data code which is not serialized.
 */
@Deprecated(
    "Objects saving themselves violates SRP, and instance method getMetadataType cannot be used at load time. " +
        "Look at MetadataRepository which does the Json serialization for you"
)
interface Saveable : JsonSerializable {

    fun getMetadataType(): Int

    fun toJson(): String
}
