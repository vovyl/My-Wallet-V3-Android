package piuk.blockchain.android.ui.dashboard

import com.blockchain.balance.TotalBalance
import com.blockchain.testutils.bitcoin
import com.blockchain.testutils.bitcoinCash
import com.blockchain.testutils.ether
import com.blockchain.testutils.gbp
import com.blockchain.testutils.lumens
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import info.blockchain.balance.CryptoCurrency
import info.blockchain.balance.CryptoValue
import info.blockchain.balance.FiatValue
import io.reactivex.Completable
import io.reactivex.Single
import org.amshove.kluent.`it returns`
import org.amshove.kluent.`should equal`
import org.junit.Test
import piuk.blockchain.androidcore.data.exchangerate.FiatExchangeRates
import java.math.BigDecimal
import java.util.Locale

class AsyncDashboardDataCalculatorTest {

    @Test
    fun `gets correct balances`() {
        val balanceUpdater = mock<BalanceUpdater> {
            on { updateBalances() } `it returns` Completable.complete()
        }
        AsyncDashboardDataCalculator(
            mockExchangeRates(
                mapOf(
                    25.bitcoin() to 100.gbp(),
                    15.bitcoin() to 200.gbp(),
                    30.bitcoinCash() to 300.gbp(),
                    5.bitcoinCash() to 400.gbp(),
                    20.ether() to 500.gbp(),
                    0.ether() to 0.gbp(),
                    100.lumens() to 600.gbp(),
                    0.lumens() to 0.gbp()
                )
            ),
            balanceUpdater,
            mockBalances(
                bitcoin = 25.bitcoin() to 15.bitcoin(),
                bitcoinCash = 30.bitcoinCash() to 5.bitcoinCash(),
                ether = 20.ether(),
                lumens = 100.lumens()
            )
        )
            .getPieChartData()
            .test()
            .assertComplete()
            .values().single() `should equal`
            PieChartsState.Data(
                bitcoin = PieChartsState.Coin(
                    spendable = dataPoint(100.gbp(), 25.bitcoin()),
                    watchOnly = dataPoint(200.gbp(), 15.bitcoin())
                ),
                bitcoinCash = PieChartsState.Coin(
                    spendable = dataPoint(300.gbp(), 30.bitcoinCash()),
                    watchOnly = dataPoint(400.gbp(), 5.bitcoinCash())
                ),
                ether = PieChartsState.Coin(
                    spendable = dataPoint(500.gbp(), 20.ether()),
                    watchOnly = dataPoint(0.gbp(), 0.ether())
                ),
                lumen = PieChartsState.Coin(
                    spendable = dataPoint(600.gbp(), 100.lumens()),
                    watchOnly = dataPoint(0.gbp(), 0.lumens())
                )
            )
        verify(balanceUpdater).updateBalances()
    }

    private fun dataPoint(fiat: FiatValue, cryptoValue: CryptoValue) =
        PieChartsState.DataPoint(fiat, cryptoValue.toStringWithSymbol(Locale.US))

    private fun mockBalances(
        bitcoin: Pair<CryptoValue, CryptoValue>,
        bitcoinCash: Pair<CryptoValue, CryptoValue>,
        ether: CryptoValue,
        lumens: CryptoValue
    ): TotalBalance =
        mock {
            on {
                balanceSpendableToWatchOnly(CryptoCurrency.BTC)
            } `it returns` Single.just(bitcoin)
            on {
                balanceSpendableToWatchOnly(CryptoCurrency.BCH)
            } `it returns` Single.just(bitcoinCash)
            on {
                balanceSpendableToWatchOnly(CryptoCurrency.ETHER)
            } `it returns` Single.just(ether to CryptoValue.ZeroEth)
            on {
                balanceSpendableToWatchOnly(CryptoCurrency.XLM)
            } `it returns` Single.just(lumens to CryptoValue.ZeroXlm)
        }

    private fun mockExchangeRates(param: Map<CryptoValue, FiatValue>): FiatExchangeRates {
        val fiatExchangeRates = mock<FiatExchangeRates>()
        whenever(fiatExchangeRates.getFiat(any())) `it returns` FiatValue.fromMajor("USD", BigDecimal.ZERO)
        param.forEach { crypto, fiat ->
            whenever(fiatExchangeRates.getFiat(crypto)) `it returns` fiat
        }
        return fiatExchangeRates
    }
}
