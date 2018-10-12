package info.blockchain.balance

import org.amshove.kluent.`should be`
import org.amshove.kluent.`should equal`
import org.junit.Before
import org.junit.Test
import java.util.Locale

class CryptoValueFromStringTest {

    @Before
    fun setUs() {
        Locale.setDefault(Locale.US)
    }

    @Before
    fun clearOther() {
        Locale.setDefault(Locale.US)
    }

    @Test
    fun `empty string`() {
        CryptoCurrency.BTC.withMajorValueOrZero("") `should equal` 0.bitcoin()
    }

    @Test
    fun `bad string`() {
        CryptoCurrency.BTC.withMajorValueOrZero("a") `should equal` 0.bitcoin()
    }

    @Test
    fun `one bitcoin`() {
        CryptoCurrency.BTC.withMajorValueOrZero("1") `should equal` 1.bitcoin()
    }

    @Test
    fun `8 dp bitcoin cash`() {
        CryptoCurrency.BCH.withMajorValueOrZero("1.12345678") `should equal` 1.12345678.bitcoinCash()
    }

    @Test
    fun `French input`() {
        Locale.setDefault(Locale.FRANCE)
        CryptoCurrency.BCH.withMajorValueOrZero("1,123") `should equal` 1.123.bitcoinCash()
    }

    @Test
    fun `UK input`() {
        Locale.setDefault(Locale.UK)
        CryptoCurrency.BCH.withMajorValueOrZero("1,123") `should equal` 1123.bitcoinCash()
    }

    @Test
    fun `Override locale input`() {
        Locale.setDefault(Locale.UK)
        CryptoCurrency.BCH.withMajorValueOrZero("1,123", Locale.FRANCE) `should equal` 1.123.bitcoinCash()
        Locale.getDefault() `should be` Locale.UK
    }

    @Test
    fun `18 dp ether`() {
        CryptoCurrency.ETHER.withMajorValueOrZero("987654321.123456789012345678") `should equal`
            "987654321.123456789012345678".toBigDecimal().ether()
    }
}
