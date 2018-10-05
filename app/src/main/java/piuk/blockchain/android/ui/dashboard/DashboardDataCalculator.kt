package piuk.blockchain.android.ui.dashboard

import info.blockchain.balance.AccountKey
import info.blockchain.balance.CryptoCurrency
import info.blockchain.balance.CryptoValue
import io.reactivex.Completable
import io.reactivex.Single
import piuk.blockchain.android.data.datamanagers.TransactionListDataManager
import piuk.blockchain.androidcore.data.bitcoincash.BchDataManager
import piuk.blockchain.androidcore.data.currency.CurrencyFormatManager
import piuk.blockchain.androidcore.data.ethereum.EthDataManager
import piuk.blockchain.androidcore.data.exchangerate.ExchangeRateDataManager
import piuk.blockchain.androidcore.data.exchangerate.toFiat
import piuk.blockchain.androidcore.data.payload.PayloadDataManager
import piuk.blockchain.androidcore.utils.PrefsUtil
import piuk.blockchain.androidcoreui.utils.logging.BalanceLoadedEvent
import piuk.blockchain.androidcoreui.utils.logging.Logging
import timber.log.Timber

internal class DashboardDataCalculator(
    private val prefsUtil: PrefsUtil,
    private val exchangeRateFactory: ExchangeRateDataManager,
    private val ethDataManager: EthDataManager,
    private val bchDataManager: BchDataManager,
    private val payloadDataManager: PayloadDataManager,
    private val transactionListDataManager: TransactionListDataManager,
    private val currencyFormatManager: CurrencyFormatManager
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

                        val fiatCurrency = prefsUtil.getValue(PrefsUtil.KEY_SELECTED_FIAT, PrefsUtil.DEFAULT_CURRENCY)

                        Logging.logCustom(
                            BalanceLoadedEvent(
                                hasBtcBalance = btcBalance.isPositive,
                                hasBchBalance = bchBalance.isPositive,
                                hasEthBalance = ethBalance.isPositive,
                                hasXlmBalance = false // TODO("AND-1503")
                            )
                        )

                        PieChartsState.Data(
                            bitcoin = PieChartsState.Coin(
                                spendable = btcBalance.toPieChartDataPoint(fiatCurrency),
                                watchOnly = btcwatchOnlyBalance.toPieChartDataPoint(fiatCurrency)
                            ),
                            bitcoinCash = PieChartsState.Coin(
                                spendable = bchBalance.toPieChartDataPoint(fiatCurrency),
                                watchOnly = bchwatchOnlyBalance.toPieChartDataPoint(fiatCurrency)
                            ),
                            ether = PieChartsState.Coin(
                                spendable = ethBalance.toPieChartDataPoint(fiatCurrency),
                                watchOnly = CryptoValue.ZeroEth.toPieChartDataPoint(
                                    fiatCurrency
                                )
                            )
                        )
                    }
            }
    }

    private fun CryptoValue.toPieChartDataPoint(fiatCurrency: String) =
        PieChartsState.DataPoint(
            fiatValue = this.toFiat(exchangeRateFactory, fiatCurrency),
            cryptoValueString = currencyFormatManager.getFormattedValueWithUnit(this)
        )
}
