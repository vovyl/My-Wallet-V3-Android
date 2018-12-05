package info.blockchain.balance

import org.amshove.kluent.`should be`
import org.amshove.kluent.`should equal`
import org.junit.Test

class FiatValueFromMinorTests {

    @Test
    fun `from minor GBP`() {
        FiatValue.fromMinor(
            "GBP",
            123
        ) `should equal` 1.23.gbp()
    }

    @Test
    fun `from minor GBP 0 end`() {
        FiatValue.fromMinor(
            "GBP",
            200
        ) `should equal` 2.gbp()
    }

    @Test
    fun `from minor GBP scale is set to 2`() {
        FiatValue.fromMinor(
            "GBP",
            200
        ).value.scale() `should be` 2
    }

    @Test
    fun `from minor USD`() {
        FiatValue.fromMinor(
            "USD",
            456
        ) `should equal` 4.56.usd()
    }

    @Test
    fun `from minor JPY`() {
        FiatValue.fromMinor(
            "JPY",
            456
        ) `should equal` 456.jpy()
    }

    @Test
    fun `from minor JPY scale is set to 0`() {
        FiatValue.fromMinor(
            "JPY",
            200
        ).value.scale() `should be` 0
    }
}
