package info.blockchain.balance

import org.amshove.kluent.`should equal`
import org.junit.Test

class FiatValueFromMajorTests {

    @Test
    fun `from major GBP`() {
        FiatValue.fromMajor(
            "GBP",
            1.23.toBigDecimal()
        ) `should equal` 1.23.gbp()
    }

    @Test
    fun `from major GBP - rounds up midway`() {
        FiatValue.fromMajor(
            "GBP",
            1.235.toBigDecimal()
        ) `should equal` 1.24.gbp()
    }

    @Test
    fun `from major GBP - rounds down below midway`() {
        FiatValue.fromMajor(
            "GBP",
            1.2349.toBigDecimal()
        ) `should equal` 1.23.gbp()
    }

    @Test
    fun `from major GBP has scale 2`() {
        FiatValue.fromMajor(
            "GBP",
            1.toBigDecimal()
        ).value.scale() `should equal` 2
    }

    @Test
    fun `from major JPY`() {
        FiatValue.fromMajor(
            "JPY",
            500.toBigDecimal()
        ) `should equal` 500.jpy()
    }

    @Test
    fun `from major JPY has scale 0`() {
        FiatValue.fromMajor(
            "JPY",
            1.toBigDecimal()
        ).value.scale() `should equal` 0
    }
}
