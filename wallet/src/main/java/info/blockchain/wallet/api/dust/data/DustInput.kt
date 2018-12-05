package info.blockchain.wallet.api.dust.data

import com.squareup.moshi.Json
import org.bitcoinj.core.NetworkParameters
import org.bitcoinj.core.Sha256Hash
import org.bitcoinj.core.TransactionOutPoint
import java.math.BigInteger

data class DustInput(
    val confirmations: Int,
    @field:Json(name = "lock_secret") val lockSecret: String,
    @field:Json(name = "output_script") val outputScript: String,
    val script: String,
    @field:Json(name = "tx_hash") val txHash: String,
    @field:Json(name = "tx_hash_big_endian") val txHashBigEndian: String,
    @field:Json(name = "tx_index") val txIndex: Long,
    @field:Json(name = "tx_output_n") val txOutputN: Long,
    val value: BigInteger,
    @field:Json(name = "value_hex") val valueHex: String
) {

    fun getTransactionOutPoint(params: NetworkParameters): TransactionOutPoint = TransactionOutPoint(
        params,
        txOutputN,
        Sha256Hash.wrap(txHashBigEndian)
    )
}