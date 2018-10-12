package com.blockchain.lockbox.data.models

import com.blockchain.serialization.JsonSerializable

data class LockboxMetadata(
    val devices: List<Device>
) : JsonSerializable {

    fun hasLockbox(): Boolean = !devices.isEmpty()

    companion object {

        const val MetaDataType = 9
    }
}

data class Device(
    val device_type: String,
    val device_name: String,
    val btc: Btc,
    val bch: Bch,
    val eth: Eth
) : JsonSerializable

data class Bch(
    val accounts: List<BitcoinLikeAccount>
) : JsonSerializable

data class Btc(
    val accounts: List<BitcoinLikeAccount>
) : JsonSerializable

data class Eth(
    val accounts: List<EthLikeAccount>,
    val last_tx: Any,
    val last_tx_timestamp: Any
) : JsonSerializable

data class BitcoinLikeAccount(
    val label: String,
    val archived: Boolean,
    val xpriv: String,
    val xpub: String,
    val address_labels: List<Any>,
    val cache: Cache
) : JsonSerializable

data class Cache(
    val receiveAccount: String,
    val changeAccount: String
) : JsonSerializable

data class EthLikeAccount(
    val label: String,
    val archived: Boolean,
    val correct: Boolean,
    val addr: String
) : JsonSerializable