package piuk.blockchain.android.ui.dashboard

import info.blockchain.balance.AccountKey
import info.blockchain.balance.CryptoCurrency
import info.blockchain.balance.CryptoValue
import info.blockchain.balance.formatWithUnit
import io.reactivex.Completable
import io.reactivex.Single
import piuk.blockchain.android.data.datamanagers.TransactionListDataManager
import piuk.blockchain.androidcore.data.bitcoincash.BchDataManager
import piuk.blockchain.androidcore.data.ethereum.EthDataManager
import piuk.blockchain.androidcore.data.exchangerate.FiatExchangeRates
import piuk.blockchain.androidcore.data.exchangerate.toFiat
import piuk.blockchain.androidcore.data.payload.PayloadDataManager
import timber.log.Timber

internal class DashboardDataCalculator(
    private val fiatExchangeRates: FiatExchangeRates,
    private val ethDataManager: EthDataManager,
    private val bchDataManager: BchDataManager,
    private val payloadDataManager: PayloadDataManager,
    private val transactionListDataManager: TransactionListDataManager
) : DashboardData {

    override fun getPieChartData(): Single<PieChartsState.Data> {
        return ethDataManager.fetchEthAddress()
            .singleOrError()
            .flatMap { ethAddressResponse ->
                payloadDataManager.updateAllBalances()
                    .andThen(
                        Completable.merge(
                            listOf(
                                payloadDataManager.updateAllTransactions(),
                                bchDataManager.updateAllBalances()
                            )
                        ).doOnError { Timber.e(it) }
                            .onErrorComplete()
                    )
                    .toSingle {
                        val btcBalance = transactionListDataManager.balance(
                            AccountKey.EntireWallet(CryptoCurrency.BTC)
                        )
                        val btcwatchOnlyBalance =
                            transactionListDataManager.balance(AccountKey.WatchOnly(CryptoCurrency.BTC))
                        val bchBalance = transactionListDataManager.balance(
                            AccountKey.EntireWallet(CryptoCurrency.BCH)
                        )
                        val bchwatchOnlyBalance =
                            transactionListDataManager.balance(AccountKey.WatchOnly(CryptoCurrency.BCH))
                        val ethBalance =
                            CryptoValue(
                                CryptoCurrency.ETHER,
                                ethAddressResponse.getTotalBalance()
                            )

                        PieChartsState.Data(
                            bitcoin = PieChartsState.Coin(
                                spendable = btcBalance.toPieChartDataPoint(fiatExchangeRates),
                                watchOnly = btcwatchOnlyBalance.toPieChartDataPoint(fiatExchangeRates)
                            ),
                            bitcoinCash = PieChartsState.Coin(
                                spendable = bchBalance.toPieChartDataPoint(fiatExchangeRates),
                                watchOnly = bchwatchOnlyBalance.toPieChartDataPoint(fiatExchangeRates)
                            ),
                            ether = PieChartsState.Coin(
                                spendable = ethBalance.toPieChartDataPoint(fiatExchangeRates),
                                watchOnly = CryptoValue.ZeroEth.toPieChartDataPoint(fiatExchangeRates)
                            ),
                            lumen = PieChartsState.Coin(
                                // TODO("AND-1540") Need real balance
                                spendable = CryptoValue.ZeroXlm.toPieChartDataPoint(fiatExchangeRates),
                                watchOnly = CryptoValue.ZeroXlm.toPieChartDataPoint(fiatExchangeRates)
                            )
                        )
                    }
            }
    }
}

internal fun CryptoValue.toPieChartDataPoint(fiatExchangeRates: FiatExchangeRates) =
    PieChartsState.DataPoint(
        fiatValue = toFiat(fiatExchangeRates),
        cryptoValueString = formatWithUnit()
    )
