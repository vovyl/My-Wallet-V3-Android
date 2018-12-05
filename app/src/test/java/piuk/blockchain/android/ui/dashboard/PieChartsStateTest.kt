package piuk.blockchain.android.ui.dashboard

import com.blockchain.testutils.gbp
import com.blockchain.testutils.usd
import org.amshove.kluent.`should be`
import org.amshove.kluent.`should equal`
import org.junit.Test
import java.util.Locale

class PieChartsStateTest {

    @Test
    fun `data point isZero`() {
        PieChartsState.DataPoint(
            fiatValue = 0.usd(),
            cryptoValueString = "anything"
        ).apply {
            isZero `should be` true
        }
    }

    @Test
    fun `data point is not Zero`() {
        PieChartsState.DataPoint(
            fiatValue = 0.01.usd(),
            cryptoValueString = "anything"
        ).apply {
            isZero `should be` false
        }
    }

    @Test
    fun `fiat value string`() {
        givenUsLocale()
        PieChartsState.DataPoint(
            fiatValue = 99.usd(),
            cryptoValueString = "anything"
        ).apply {
            fiatValueString `should equal` "$99.00"
        }
    }

    @Test
    fun `fiat value string - UK`() {
        givenUkLocale()
        PieChartsState.DataPoint(
            fiatValue = 88.gbp(),
            cryptoValueString = "anything"
        ).apply {
            fiatValueString `should equal` "£88.00"
        }
    }

    @Test
    fun `can calculate total value`() {
        givenUsLocale()
        val i = 100
        PieChartsState.Data(
            bitcoin = PieChartsState.Coin(
                spendable = PieChartsState.DataPoint(
                    fiatValue = i.usd(),
                    cryptoValueString = "1 BTC"
                ),
                watchOnly = zeroDataPoint()
            ),
            bitcoinCash = PieChartsState.Coin(
                spendable = PieChartsState.DataPoint(
                    fiatValue = 200.usd(),
                    cryptoValueString = "2 BCH"
                ),
                watchOnly = zeroDataPoint()
            ),
            ether = PieChartsState.Coin(
                spendable = PieChartsState.DataPoint(
                    fiatValue = 300.usd(),
                    cryptoValueString = "3 ETH"
                ),
                watchOnly = zeroDataPoint()
            )
        ).apply {
            totalValueString `should equal` "$600.00"
        }
    }

    @Test
    fun `all zero`() {
        givenUsLocale()
        PieChartsState.Data(
            bitcoin = zeroCoin(),
            bitcoinCash = zeroCoin(),
            ether = zeroCoin()
        ).apply {
            isZero `should be` true
        }
    }

    @Test
    fun `btc not zero`() {
        givenUsLocale()
        PieChartsState.Data(
            bitcoin = nonZeroCoin(),
            bitcoinCash = zeroCoin(),
            ether = zeroCoin()
        ).apply {
            isZero `should be` false
        }
    }

    @Test
    fun `bch not zero`() {
        givenUsLocale()
        PieChartsState.Data(
            bitcoin = zeroCoin(),
            bitcoinCash = nonZeroCoin(),
            ether = zeroCoin()
        ).apply {
            isZero `should be` false
        }
    }

    @Test
    fun `eth not zero`() {
        givenUsLocale()
        PieChartsState.Data(
            bitcoin = zeroCoin(),
            bitcoinCash = zeroCoin(),
            ether = nonZeroCoin()
        ).apply {
            isZero `should be` false
        }
    }

    @Test
    fun `bitcoin watch-only not zero`() {
        givenUsLocale()
        PieChartsState.Data(
            bitcoin = nonZeroWatchOnlyCoin(),
            bitcoinCash = zeroCoin(),
            ether = zeroCoin()
        ).apply {
            isZero `should be` false
        }
    }

    @Test
    fun `bitcoin cash watch-only not zero`() {
        givenUsLocale()
        PieChartsState.Data(
            bitcoin = zeroCoin(),
            bitcoinCash = nonZeroWatchOnlyCoin(),
            ether = zeroCoin()
        ).apply {
            isZero `should be` false
        }
    }

    @Test
    fun `ether watch-only not zero`() {
        givenUsLocale()
        PieChartsState.Data(
            bitcoin = zeroCoin(),
            bitcoinCash = zeroCoin(),
            ether = nonZeroWatchOnlyCoin()
        ).apply {
            isZero `should be` false
        }
    }

    @Test
    fun `coin watch-only not zero`() {
        nonZeroWatchOnlyCoin()
            .isZero `should be` false
    }

    private fun zeroCoin() =
        PieChartsState.Coin(
            spendable = zeroDataPoint(),
            watchOnly = zeroDataPoint()
        )

    private fun nonZeroCoin() =
        PieChartsState.Coin(
            spendable = nonZeroDataPoint(),
            watchOnly = zeroDataPoint()
        )

    private fun nonZeroWatchOnlyCoin() =
        PieChartsState.Coin(
            spendable = zeroDataPoint(),
            watchOnly = nonZeroDataPoint()
        )

    private fun nonZeroDataPoint(): PieChartsState.DataPoint {
        return PieChartsState.DataPoint(
            fiatValue = 1.usd(),
            cryptoValueString = "anything"
        )
    }

    private fun zeroDataPoint(): PieChartsState.DataPoint {
        return PieChartsState.DataPoint(
            fiatValue = 0.usd(),
            cryptoValueString = "anything"
        )
    }

    private fun givenUsLocale() {
        Locale.setDefault(Locale.US)
    }

    private fun givenUkLocale() {
        Locale.setDefault(Locale.UK)
    }
}