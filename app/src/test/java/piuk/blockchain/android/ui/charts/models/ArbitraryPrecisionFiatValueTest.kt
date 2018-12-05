package piuk.blockchain.android.ui.charts.models

import org.amshove.kluent.`should equal`
import org.junit.Test
import java.util.Locale

class ArbitraryPrecisionFiatValueTest {

    @Test
    fun `retains source precision`() {
        ArbitraryPrecisionFiatValue.fromMajor(
            "GBP",
            1.23456789.toBigDecimal()
        ).value `should equal` 1.23456789.toBigDecimal()
    }

    @Test
    fun `returns string with source precision`() {
        ArbitraryPrecisionFiatValue.fromMajor(
            "USD",
            1000.23456789.toBigDecimal()
        ).toStringWithSymbol(Locale.US) `should equal` "$1,000.23456789"
    }

    @Test
    fun `returns string with enforced minimum precision`() {
        ArbitraryPrecisionFiatValue.fromMajor(
            "USD",
            1.toBigDecimal()
        ).toStringWithSymbol(Locale.US) `should equal` "$1.00"
    }

    @Test
    fun `returns string with source precision ignoring extra zeros`() {
        ArbitraryPrecisionFiatValue.fromMajor(
            "USD",
            0.00000100000.toBigDecimal()
        ).toStringWithSymbol(Locale.US) `should equal` "$0.000001"
    }

    @Test
    fun `returns string respecting FR locale`() {
        ArbitraryPrecisionFiatValue.fromMajor(
            "USD",
            12.34567.toBigDecimal()
        ).toStringWithSymbol(Locale.FRANCE) `should equal` "12,34567 USD"
    }
}