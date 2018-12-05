package com.blockchain.sunriver.datamanager

import com.blockchain.serialization.JsonSerializable
import com.blockchain.serialization.fromMoshiJson
import com.blockchain.serialization.toMoshiJson
import com.blockchain.testutils.`should be assignable from`
import com.blockchain.testutils.getStringFromResource
import org.amshove.kluent.`should be`
import org.amshove.kluent.`should equal`
import org.junit.Test

class XlmMetaDataSerializationTest {

    @Test
    fun `XlmAccount is JsonSerializable`() {
        JsonSerializable::class `should be assignable from` XlmAccount::class
    }

    @Test
    fun `XlmMetadata is JsonSerializable`() {
        JsonSerializable::class `should be assignable from` XlmMetaData::class
    }

    @Test
    fun `can deserialize`() {
        XlmMetaData::class.fromMoshiJson(getStringFromResource("metadata/xlm_metadata.json"))
            .apply {
                defaultAccountIndex `should be` 0
                accounts `should equal` listOf(
                    XlmAccount(
                        "GBCRNVZPJFDBF3JECAXOTD2LTAQMLHVYJUV5IEGYJ5H73TA3EOW7RZJY",
                        label = "My Stellar Wallet",
                        archived = false
                    )
                )
                transactionNotes `should equal` emptyMap<String, String>()
            }
    }

    @Test
    fun `can deserialize alternative values`() {
        XlmMetaData::class.fromMoshiJson(getStringFromResource("metadata/xlm_metadata_2.json"))
            .apply {
                defaultAccountIndex `should be` 1
                accounts `should equal` listOf(
                    XlmAccount(
                        "GBNPUQCB2UY7YXBKZZYMRXDH3WMVD6XOGOHAU5U4WIXOPHKN3TRBXD2Z",
                        label = "My Old Stellar Wallet",
                        archived = true
                    ),
                    XlmAccount(
                        "GDTDFKFRZHTSGQGCSRWLJWCTR5BPM6LBLMQQ75G3DR4DANLDY73CTNU4",
                        label = "My New Stellar Wallet",
                        archived = false
                    )
                )
                transactionNotes `should equal` mapOf(
                    "tx1" to "something",
                    "tx2" to "something else"
                )
            }
    }

    @Test
    fun `can deserialize missing values`() {
        XlmMetaData::class.fromMoshiJson(getStringFromResource("metadata/xlm_metadata_with_missing_values.json"))
            .apply {
                defaultAccountIndex `should be` 0
                accounts `should equal` listOf(
                    XlmAccount(
                        "GBNPUQCB2UY7YXBKZZYMRXDH3WMVD6XOGOHAU5U4WIXOPHKN3TRBXD2Z",
                        label = null,
                        archived = false
                    ),
                    XlmAccount(
                        "GDTDFKFRZHTSGQGCSRWLJWCTR5BPM6LBLMQQ75G3DR4DANLDY73CTNU4",
                        label = null,
                        archived = false
                    )
                )
                transactionNotes `should be` null
            }
    }

    @Test
    fun `can deserialize missing everything`() {
        XlmMetaData::class.fromMoshiJson(json = "{}")
            .apply {
                defaultAccountIndex `should be` 0
                accounts `should be` null
                transactionNotes `should be` null
            }
    }

    @Test
    fun `can round trip`() {
        getStringFromResource("metadata/xlm_metadata.json")
            .assertJsonRoundTrip()
    }

    @Test
    fun `can round trip alternative values`() {
        getStringFromResource("metadata/xlm_metadata_2.json")
            .assertJsonRoundTrip()
    }

    @Test
    fun `can round trip with missing`() {
        getStringFromResource("metadata/xlm_metadata_with_missing_values.json")
            .assertJsonRoundTrip()
    }

    @Test
    fun `can round trip missing everything`() {
        "{}".assertJsonRoundTrip()
    }

    @Test
    fun `Meta data type`() {
        XlmMetaData.MetaDataType `should be` 11
    }
}

private fun String.assertJsonRoundTrip() {
    XlmMetaData::class.fromMoshiJson(this).assertJsonRoundTrip()
}

private fun XlmMetaData.assertJsonRoundTrip() {
    XlmMetaData::class.fromMoshiJson(toMoshiJson()) `should equal` this
}
