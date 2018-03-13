package piuk.blockchain.android.data.currency

import info.blockchain.wallet.prices.PriceApi
import io.reactivex.Observable
import piuk.blockchain.android.data.rxjava.RxUtil
import piuk.blockchain.android.util.annotations.WebRequest
import java.math.BigDecimal
import java.math.BigInteger

class ExchangeRateService(val priceApi: PriceApi) {

    @WebRequest
    fun getBtcExchangeRateObservable() =
            priceApi.getPriceIndexes(CryptoCurrencies.BTC.symbol)
                    .compose(RxUtil.applySchedulersToObservable())

    @WebRequest
    fun getEthExchangeRateObservable() =
            priceApi.getPriceIndexes(CryptoCurrencies.ETHER.symbol)
                    .compose(RxUtil.applySchedulersToObservable())

    @WebRequest
    fun getBchExchangeRateObservable() =
            priceApi.getPriceIndexes(CryptoCurrencies.BCH.symbol)
                    .compose(RxUtil.applySchedulersToObservable())

    /**
     * Returns the historic value of a number of Satoshi at a given time in a given currency.
     *
     * @param satoshis     The amount of Satoshi to be converted
     * @param currency     The currency to be converted to as a 3 letter acronym, eg USD, GBP
     * @param timeInSeconds The time at which to get the price, in seconds since epoch
     * @return A double value, which <b>is not</b> rounded to any significant figures
     */
    @WebRequest
    fun getBtcHistoricPrice(
            satoshis: Long,
            currency: String,
            timeInSeconds: Long
    ): Observable<Double> =
            priceApi.getHistoricPrice(CryptoCurrencies.BTC.symbol, currency, timeInSeconds)
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
    @WebRequest
    fun getEthHistoricPrice(
            wei: BigInteger,
            currency: String,
            timeInSeconds: Long
    ): Observable<Double> =
            priceApi.getHistoricPrice(CryptoCurrencies.ETHER.symbol, currency, timeInSeconds)
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
    @WebRequest
    fun getBchHistoricPrice(
            satoshis: Long,
            currency: String,
            timeInSeconds: Long
    ): Observable<Double> =
            priceApi.getHistoricPrice(CryptoCurrencies.BCH.symbol, currency, timeInSeconds)
                    .map {
                        val exchangeRate = BigDecimal.valueOf(it)
                        val satoshiDecimal = BigDecimal.valueOf(satoshis)
                        return@map exchangeRate.multiply(satoshiDecimal.divide(ExchangeRateDataManager.SATOSHIS_PER_BITCOIN))
                                .toDouble()
                    }
                    .compose(RxUtil.applySchedulersToObservable())
}