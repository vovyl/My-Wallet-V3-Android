package piuk.blockchain.androidcore.data.exchangerate.datastore

import info.blockchain.balance.CryptoCurrency
import info.blockchain.wallet.prices.data.PriceDatum
import io.reactivex.Completable
import io.reactivex.Single
import piuk.blockchain.androidcore.data.exchangerate.ExchangeRateService
import piuk.blockchain.androidcore.utils.PrefsUtil
import timber.log.Timber
import java.math.BigDecimal

class ExchangeRateDataStore(
    private val exchangeRateService: ExchangeRateService,
    private val prefsUtil: PrefsUtil
) {

    // Ticker data
    private var btcTickerData: Map<String, PriceDatum>? = null
    private var ethTickerData: Map<String, PriceDatum>? = null
    private var bchTickerData: Map<String, PriceDatum>? = null
    private var xlmTickerData: Map<String, PriceDatum>? = null

    fun updateExchangeRates(): Completable = Single.merge(
        exchangeRateService.getExchangeRateMap(CryptoCurrency.BTC)
            .doOnSuccess { btcTickerData = it.toMap() },
        exchangeRateService.getExchangeRateMap(CryptoCurrency.BCH)
            .doOnSuccess { bchTickerData = it.toMap() },
        exchangeRateService.getExchangeRateMap(CryptoCurrency.ETHER)
            .doOnSuccess { ethTickerData = it.toMap() },
        exchangeRateService.getExchangeRateMap(CryptoCurrency.XLM)
            .doOnSuccess { xlmTickerData = it.toMap() }
    ).ignoreElements()

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
            CryptoCurrency.BTC -> btcTickerData
            CryptoCurrency.ETHER -> ethTickerData
            CryptoCurrency.BCH -> bchTickerData
            CryptoCurrency.XLM -> xlmTickerData
        }

    fun getHistoricPrice(cryptoCurrency: CryptoCurrency, fiat: String, timeInSeconds: Long): Single<BigDecimal> =
        exchangeRateService.getHistoricPrice(cryptoCurrency, fiat, timeInSeconds)
            .map { it.toBigDecimal() }
}
