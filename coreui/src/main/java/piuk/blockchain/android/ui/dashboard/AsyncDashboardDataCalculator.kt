package piuk.blockchain.android.ui.dashboard

import com.blockchain.balance.TotalBalance
import info.blockchain.balance.CryptoCurrency
import info.blockchain.balance.CryptoValue
import info.blockchain.balance.formatWithUnit
import io.reactivex.Single
import io.reactivex.rxkotlin.toObservable

import piuk.blockchain.androidcore.data.exchangerate.FiatExchangeRates
import piuk.blockchain.androidcore.data.exchangerate.toFiat

class AsyncDashboardDataCalculator(
    private val fiatExchangeRates: FiatExchangeRates,
    private val balanceUpdater: BalanceUpdater,
    private val totalBalance: TotalBalance
) : DashboardData {

    override fun getPieChartData(): Single<PieChartsState.Data> =
        balanceUpdater.updateBalances()
            .toSingle {
                pieChartData()
            }.flatMap { it }

    private fun pieChartData() =
        DashboardConfig.currencies.toObservable()
            .flatMapSingle {
                totalBalance.balanceSpendableToWatchOnly(it)
            }
            .toMap(
                { it.first.currency },
                { it.toPieChartCoin(fiatExchangeRates) }
            )
            .map {
                PieChartsState.Data(
                    bitcoin = it.coin(CryptoCurrency.BTC),
                    bitcoinCash = it.coin(CryptoCurrency.BCH),
                    ether = it.coin(CryptoCurrency.ETHER),
                    lumen = it.coin(CryptoCurrency.XLM)
                )
            }

    private fun Map<CryptoCurrency, PieChartsState.Coin>.coin(btc: CryptoCurrency) =
        this[btc] ?: zeroCoin(btc, fiatExchangeRates)
}

fun zeroCoin(currency: CryptoCurrency, fiatExchangeRates: FiatExchangeRates): PieChartsState.Coin {
    val zero = CryptoValue.zero(currency)
    return (zero to zero).toPieChartCoin(fiatExchangeRates)
}

private fun Pair<CryptoValue, CryptoValue>.toPieChartCoin(fiatExchangeRates: FiatExchangeRates) =
    PieChartsState.Coin(
        spendable = this.first.toPieChartDataPoint(fiatExchangeRates),
        watchOnly = this.second.toPieChartDataPoint(fiatExchangeRates)
    )

fun CryptoValue.toPieChartDataPoint(fiatExchangeRates: FiatExchangeRates) =
    PieChartsState.DataPoint(
        fiatValue = toFiat(fiatExchangeRates),
        cryptoValueString = formatWithUnit()
    )
