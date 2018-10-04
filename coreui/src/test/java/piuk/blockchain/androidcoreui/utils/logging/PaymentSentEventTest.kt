package piuk.blockchain.androidcoreui.utils.logging

import com.blockchain.testutils.bitcoin
import com.blockchain.testutils.bitcoinCash
import com.blockchain.testutils.ether
import com.blockchain.testutils.lumens
import org.amshove.kluent.`should equal`
import org.junit.Test

class PaymentSentEventTest {

    @Test
    fun `all ranges of bitcoin`() {
        listOf(
            0.049_999_99.bitcoin() to "0 - 0.05 BTC",
            0.05.bitcoin() to "0.05 - 0.1 BTC",
            0.099_999_99.bitcoin() to "0.05 - 0.1 BTC",
            0.1.bitcoin() to "0.1 - 0.5 BTC",
            0.499_999_99.bitcoin() to "0.1 - 0.5 BTC",
            0.5.bitcoin() to "0.5 - 1.0 BTC",
            0.999_999_99.bitcoin() to "0.5 - 1.0 BTC",
            1.bitcoin() to "1.0 - 10 BTC",
            9.999_999_99.bitcoin() to "1.0 - 10 BTC",
            10.bitcoin() to "10 - 100 BTC",
            99.999_999_99.bitcoin() to "10 - 100 BTC",
            100.bitcoin() to "100 - 1000 BTC",
            999.999_999_99.bitcoin() to "100 - 1000 BTC",
            1_000.bitcoin() to "1000 BTC",
            1_000_000.bitcoin() to "1000 BTC"
        ).forEach { (value, expected) ->
            PaymentSentEvent()
                .putAmountForRange(value)
                .buildToMap()
                .also {
                    it["Amount"] `should equal` expected
                    it["Currency"] `should equal` "BTC"
                }
        }
    }

    @Test
    fun `all ranges of bitcoinCash`() {
        listOf(
            0.049_999_99.bitcoinCash() to "0 - 0.05 BCH",
            0.05.bitcoinCash() to "0.05 - 0.1 BCH",
            0.099_999_99.bitcoinCash() to "0.05 - 0.1 BCH",
            0.1.bitcoinCash() to "0.1 - 0.5 BCH",
            0.499_999_99.bitcoinCash() to "0.1 - 0.5 BCH",
            0.5.bitcoinCash() to "0.5 - 1.0 BCH",
            0.999_999_99.bitcoinCash() to "0.5 - 1.0 BCH",
            1.bitcoinCash() to "1.0 - 10 BCH",
            9.999_999_99.bitcoinCash() to "1.0 - 10 BCH",
            10.bitcoinCash() to "10 - 100 BCH",
            99.999_999_99.bitcoinCash() to "10 - 100 BCH",
            100.bitcoinCash() to "100 - 1000 BCH",
            999.999_999_99.bitcoinCash() to "100 - 1000 BCH",
            1_000.bitcoinCash() to "1000 BCH",
            1_000_000.bitcoinCash() to "1000 BCH"
        ).forEach { (value, expected) ->
            PaymentSentEvent()
                .putAmountForRange(value)
                .buildToMap()
                .also {
                    it["Amount"] `should equal` expected
                    it["Currency"] `should equal` "BCH"
                }
        }
    }

    @Test
    fun `all ranges of Ethereum`() {
        listOf(
            0.049_999_99.ether() to "0 - 0.05 ETH",
            0.05.ether() to "0.05 - 0.1 ETH",
            0.099_999_99.ether() to "0.05 - 0.1 ETH",
            0.1.ether() to "0.1 - 0.5 ETH",
            0.499_999_99.ether() to "0.1 - 0.5 ETH",
            0.5.ether() to "0.5 - 1.0 ETH",
            0.999_999_99.ether() to "0.5 - 1.0 ETH",
            1.ether() to "1.0 - 10 ETH",
            9.999_999_99.ether() to "1.0 - 10 ETH",
            10.ether() to "10 - 100 ETH",
            99.999_999_99.ether() to "10 - 100 ETH",
            100.ether() to "100 - 1000 ETH",
            999.999_999_99.ether() to "100 - 1000 ETH",
            1_000.ether() to "1000 ETH",
            1_000_000.ether() to "1000 ETH"
        ).forEach { (value, expected) ->
            PaymentSentEvent()
                .putAmountForRange(value)
                .buildToMap()
                .also {
                    it["Amount"] `should equal` expected
                    it["Currency"] `should equal` "ETH"
                }
        }
    }

    @Test
    fun `all ranges of XLM`() {
        listOf(
            0.049_999_99.lumens() to "0 - 0.05 XLM",
            0.05.lumens() to "0.05 - 0.1 XLM",
            0.099_999_99.lumens() to "0.05 - 0.1 XLM",
            0.1.lumens() to "0.1 - 0.5 XLM",
            0.499_999_99.lumens() to "0.1 - 0.5 XLM",
            0.5.lumens() to "0.5 - 1.0 XLM",
            0.999_999_99.lumens() to "0.5 - 1.0 XLM",
            1.lumens() to "1.0 - 10 XLM",
            9.999_999_99.lumens() to "1.0 - 10 XLM",
            10.lumens() to "10 - 100 XLM",
            99.999_999_99.lumens() to "10 - 100 XLM",
            100.lumens() to "100 - 1000 XLM",
            999.999_999_99.lumens() to "100 - 1000 XLM",
            1_000.lumens() to "1000 XLM",
            1_000_000.lumens() to "1000 XLM"
        ).forEach { (value, expected) ->
            PaymentSentEvent()
                .putAmountForRange(value)
                .buildToMap()
                .also {
                    it["Amount"] `should equal` expected
                    it["Currency"] `should equal` "XLM"
                }
        }
    }

    private fun PaymentSentEvent.buildToMap(): Map<String, String> {
        val map = mutableMapOf<String, String>()
        build { key, value -> map[key] = value }
        return map
    }
}
