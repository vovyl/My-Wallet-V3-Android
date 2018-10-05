package piuk.blockchain.androidcore.data.exchangerate.datastore

import info.blockchain.balance.CryptoCurrency
import info.blockchain.wallet.prices.data.PriceDatum
import io.reactivex.Completable
import io.reactivex.Observable
import piuk.blockchain.androidcore.data.exchangerate.ExchangeRateService
import piuk.blockchain.androidcore.utils.PrefsUtil
import piuk.blockchain.androidcore.utils.extensions.applySchedulers
import timber.log.Timber

class ExchangeRateDataStore(
    private val exchangeRateService: ExchangeRateService,
    private val prefsUtil: PrefsUtil
) {

    // Ticker data
    private var btcTickerData: Map<String, PriceDatum>? = null
    private var ethTickerData: Map<String, PriceDatum>? = null
    private var bchTickerData: Map<String, PriceDatum>? = null
    private var xlmTickerData: Map<String, PriceDatum>? = null

    private fun btcExchangeRateObservable() =
        exchangeRateService.getBtcExchangeRateObservable()
            .doOnNext { btcTickerData = it.toMap() }

    private fun bchExchangeRateObservable() =
        exchangeRateService.getBchExchangeRateObservable()
            .doOnNext { bchTickerData = it.toMap() }

    private fun ethExchangeRateObservable() =
        exchangeRateService.getEthExchangeRateObservable()
            .doOnNext { ethTickerData = it.toMap() }

    fun updateExchangeRates(): Completable =
        Completable.fromObservable(
            Observable.merge(
                btcExchangeRateObservable(),
                bchExchangeRateObservable(),
                ethExchangeRateObservable()
                // TODO("AND-1525") Need XLM exchange observable here
            ).applySchedulers()
        )

    fun getCurrencyLabels(): Array<String> = btcTickerData!!.keys.toTypedArray()

    fun getLastPrice(cryptoCurrency: CryptoCurrency, currencyName: String): Double {
        var currency = currencyName
        if (currency.isEmpty()) {
            currency = "USD"
        }

        val tickerData = cryptoCurrency.tickerData()

        val prefsKey = "LAST_KNOWN_${cryptoCurrency.symbol}_VALUE_FOR_CURRENCY_$currency"

        val lastKnown = try {
            prefsUtil.getValue(prefsKey, "0.0").toDouble()
        } catch (e: NumberFormatException) {
            Timber.e(e)
            prefsUtil.setValue(prefsKey, "0.0")
            0.0
        }

        val lastPrice: Double? = tickerData?.get(currency)?.price

        if (lastPrice != null) {
            prefsUtil.setValue("$prefsKey$currency", lastPrice.toString())
        }

        return lastPrice ?: lastKnown
    }

    private fun CryptoCurrency.tickerData() =
        when (this) {
            CryptoCurrency.BTC -> {
                btcTickerData
            }
            CryptoCurrency.ETHER -> {
                ethTickerData
            }
            CryptoCurrency.BCH -> {
                bchTickerData
            }
            CryptoCurrency.XLM -> {
                xlmTickerData
            }
        }

    fun getBtcHistoricPrice(
        currency: String,
        timeInSeconds: Long
    ) = exchangeRateService.getBtcHistoricPrice(currency, timeInSeconds)
        .applySchedulers()

    fun getEthHistoricPrice(
        currency: String,
        timeInSeconds: Long
    ): Observable<Double> =
        exchangeRateService.getEthHistoricPrice(currency, timeInSeconds)
            .applySchedulers()

    fun getBchHistoricPrice(
        currency: String,
        timeInSeconds: Long
    ): Observable<Double> =
        exchangeRateService.getBchHistoricPrice(currency, timeInSeconds)
            .applySchedulers()
}
