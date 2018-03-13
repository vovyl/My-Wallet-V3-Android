package piuk.blockchain.android.data.currency.datastore

import info.blockchain.wallet.prices.data.PriceDatum
import io.reactivex.Completable
import io.reactivex.Observable
import piuk.blockchain.android.data.currency.CryptoCurrencies
import piuk.blockchain.android.data.currency.ExchangeRateDataManager
import piuk.blockchain.android.data.currency.ExchangeRateService
import piuk.blockchain.android.data.rxjava.RxUtil
import piuk.blockchain.android.data.stores.Optional
import piuk.blockchain.android.util.PrefsUtil
import piuk.blockchain.android.util.annotations.Mockable
import timber.log.Timber
import java.math.BigDecimal
import java.math.BigInteger

@Mockable
class ExchangeRateDataStore(private val exchangeRateService: ExchangeRateService,
                            private val prefsUtil: PrefsUtil) {

    // Ticker data
    private var btcTickerData: Map<String, PriceDatum>? = null
    private var ethTickerData: Map<String, PriceDatum>? = null
    private var bchTickerData: Map<String, PriceDatum>? = null


    private fun btcExchangeRateObservable() =
            exchangeRateService.getBtcExchangeRateObservable()
                    .doOnNext { btcTickerData = it.toMap() }

    private fun bchExchangeRateObservable() =
            exchangeRateService.getBchExchangeRateObservable()
                    .doOnNext { bchTickerData = it.toMap() }

    private fun ethExchangeRateObservable() =
            exchangeRateService.getEthExchangeRateObservable()
                    .doOnNext { ethTickerData = it.toMap() }

    fun updateExchangeRates() =
            Completable.fromObservable(Observable.merge(
                    btcExchangeRateObservable(),
                    bchExchangeRateObservable(),
                    ethExchangeRateObservable())
                    .compose(RxUtil.applySchedulersToObservable()))

    fun getCurrencyLabels(): Array<String> = btcTickerData!!.keys.toTypedArray()

    fun getLastBtcPrice(currencyName: String) =
            getLastPrice(currencyName.toUpperCase(), CryptoCurrencies.BTC)

    fun getLastBchPrice(currencyName: String) =
            getLastPrice(currencyName.toUpperCase(), CryptoCurrencies.BCH)

    fun getLastEthPrice(currencyName: String) =
            getLastPrice(currencyName.toUpperCase(), CryptoCurrencies.ETHER)

    private fun getLastPrice(currencyName: String, cryptoCurrency: CryptoCurrencies): Double {
        val prefsKey: String
        val tickerData: Map<String, PriceDatum>?

        when (cryptoCurrency) {
            CryptoCurrencies.BTC -> {
                prefsKey = PREF_LAST_KNOWN_BTC_PRICE
                tickerData = btcTickerData
            }
            CryptoCurrencies.ETHER -> {
                prefsKey = PREF_LAST_KNOWN_ETH_PRICE
                tickerData = ethTickerData
            }
            CryptoCurrencies.BCH -> {
                prefsKey = PREF_LAST_KNOWN_BCH_PRICE
                tickerData = bchTickerData
            }
        }
        var currency = currencyName
        if (currency.isEmpty()) {
            currency = "USD"
        }

        var lastPrice: Double
        val lastKnown = try {
            prefsUtil.getValue("$prefsKey$currency", "0.0").toDouble()
        } catch (e: NumberFormatException) {
            Timber.e(e)
            prefsUtil.setValue("$prefsKey$currency", "0.0")
            0.0
        }

        if (tickerData == null) {
            lastPrice = lastKnown
        } else {
            val tickerItem = when (cryptoCurrency) {
                CryptoCurrencies.BTC -> getTickerItem(currency, btcTickerData)
                CryptoCurrencies.ETHER -> getTickerItem(currency, ethTickerData)
                CryptoCurrencies.BCH -> getTickerItem(currency, bchTickerData)
            }

            lastPrice = when (tickerItem) {
                is Optional.Some -> tickerItem.element.price ?: 0.0
                else -> 0.0
            }

            if (lastPrice > 0.0) {
                prefsUtil.setValue("$prefsKey$currency", lastPrice.toString())
            } else {
                lastPrice = lastKnown
            }
        }

        return lastPrice
    }

    private fun getTickerItem(
            currencyName: String,
            tickerData: Map<String, PriceDatum>?
    ): Optional<PriceDatum> {
        val priceDatum = tickerData?.get(currencyName)
        return when {
            priceDatum != null -> Optional.Some(priceDatum)
            else -> Optional.None
        }
    }

    /**
     * Returns the historic value of a number of Satoshi at a given time in a given currency.
     *
     * @param satoshis     The amount of Satoshi to be converted
     * @param currency     The currency to be converted to as a 3 letter acronym, eg USD, GBP
     * @param timeInSeconds The time at which to get the price, in seconds since epoch
     * @return A double value, which <b>is not</b> rounded to any significant figures
     */
    fun getBtcHistoricPrice(
            satoshis: Long,
            currency: String,
            timeInSeconds: Long
    ): Observable<Double> =
            exchangeRateService.getBtcHistoricPrice(satoshis, currency, timeInSeconds)
                    .map {
                        val exchangeRate = BigDecimal.valueOf(it)
                        val satoshiDecimal = BigDecimal.valueOf(satoshis)
                        return@map exchangeRate.multiply(satoshiDecimal.divide(ExchangeRateDataManager.SATOSHIS_PER_BITCOIN))
                                .toDouble()
                    }
                    .compose(RxUtil.applySchedulersToObservable())

    /**
     * Returns the historic value of a number of Wei at a given time in a given currency.
     *
     * @param wei          The amount of Ether to be converted in Wei, ie ETH * 1e18
     * @param currency     The currency to be converted to as a 3 letter acronym, eg USD, GBP
     * @param timeInSeconds The time at which to get the price, in seconds since epoch
     * @return A double value, which <b>is not</b> rounded to any significant figures
     */
    fun getEthHistoricPrice(
            wei: BigInteger,
            currency: String,
            timeInSeconds: Long
    ): Observable<Double> =
            exchangeRateService.getEthHistoricPrice(wei, currency, timeInSeconds)
                    .map {
                        val exchangeRate = BigDecimal.valueOf(it)
                        val ethDecimal = BigDecimal(wei)
                        return@map exchangeRate.multiply(ethDecimal.divide(ExchangeRateDataManager.WEI_PER_ETHER))
                                .toDouble()
                    }
                    .compose(RxUtil.applySchedulersToObservable())

    /**
     * Returns the historic value of a number of Satoshi at a given time in a given currency.
     *
     * @param satoshis     The amount of Satoshi to be converted
     * @param currency     The currency to be converted to as a 3 letter acronym, eg USD, GBP
     * @param timeInSeconds The time at which to get the price, in seconds since epoch
     * @return A double value, which <b>is not</b> rounded to any significant figures
     */
    fun getBchHistoricPrice(
            satoshis: Long,
            currency: String,
            timeInSeconds: Long
    ): Observable<Double> =
            exchangeRateService.getBchHistoricPrice(satoshis, currency, timeInSeconds)
                    .map {
                        val exchangeRate = BigDecimal.valueOf(it)
                        val satoshiDecimal = BigDecimal.valueOf(satoshis)
                        return@map exchangeRate.multiply(satoshiDecimal.divide(ExchangeRateDataManager.SATOSHIS_PER_BITCOIN))
                                .toDouble()
                    }
                    .compose(RxUtil.applySchedulersToObservable())

    companion object {
        private const val PREF_LAST_KNOWN_BTC_PRICE = "LAST_KNOWN_BTC_VALUE_FOR_CURRENCY_"
        private const val PREF_LAST_KNOWN_ETH_PRICE = "LAST_KNOWN_ETH_VALUE_FOR_CURRENCY_"
        private const val PREF_LAST_KNOWN_BCH_PRICE = "LAST_KNOWN_BCH_VALUE_FOR_CURRENCY_"
    }
}