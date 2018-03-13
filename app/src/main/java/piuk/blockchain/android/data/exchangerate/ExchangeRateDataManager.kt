package piuk.blockchain.android.data.exchangerate

import io.reactivex.Observable
import piuk.blockchain.android.data.exchangerate.datastore.ExchangeRateDataStore
import piuk.blockchain.android.data.rxjava.RxBus
import piuk.blockchain.android.data.rxjava.RxPinning
import piuk.blockchain.android.data.rxjava.RxUtil
import piuk.blockchain.android.util.annotations.Mockable
import java.math.BigDecimal
import java.math.BigInteger

@Mockable
class ExchangeRateDataManager(val exchangeRateDataStore: ExchangeRateDataStore,
                              val rxBus: RxBus) {

    private final val rxPinning = RxPinning(rxBus)

    fun updateTickers() =
            rxPinning.call { exchangeRateDataStore.updateExchangeRates() }
                    .compose(RxUtil.applySchedulersToCompletable())

    fun getLastBtcPrice(currencyName: String) =
            exchangeRateDataStore.getLastBtcPrice(currencyName)

    fun getLastBchPrice(currencyName: String) =
            exchangeRateDataStore.getLastBchPrice(currencyName)

    fun getLastEthPrice(currencyName: String) =
            exchangeRateDataStore.getLastEthPrice(currencyName)

    fun getCurrencyLabels() = exchangeRateDataStore.getCurrencyLabels()

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
    ): Observable<Double> = rxPinning.call<Double> {
        exchangeRateDataStore.getBtcHistoricPrice(currency, timeInSeconds)
                .map {
                    val exchangeRate = BigDecimal.valueOf(it)
                    val satoshiDecimal = BigDecimal.valueOf(satoshis)
                    return@map exchangeRate.multiply(satoshiDecimal.divide(SATOSHIS_PER_BITCOIN))
                            .toDouble()
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
    fun getBchHistoricPrice(
            satoshis: Long,
            currency: String,
            timeInSeconds: Long
    ): Observable<Double> = rxPinning.call<Double> {
        exchangeRateDataStore.getBchHistoricPrice(currency, timeInSeconds)
                .map {
                    val exchangeRate = BigDecimal.valueOf(it)
                    val satoshiDecimal = BigDecimal.valueOf(satoshis)
                    return@map exchangeRate.multiply(satoshiDecimal.divide(SATOSHIS_PER_BITCOIN))
                            .toDouble()
                }
    }

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
    ): Observable<Double> = rxPinning.call<Double> {
        exchangeRateDataStore.getEthHistoricPrice(currency, timeInSeconds)
                .map {
                    val exchangeRate = BigDecimal.valueOf(it)
                    val ethDecimal = BigDecimal(wei)
                    return@map exchangeRate.multiply(ethDecimal.divide(WEI_PER_ETHER))
                            .toDouble()
                }
    }

    companion object {
        internal val SATOSHIS_PER_BITCOIN = BigDecimal.valueOf(100_000_000L)
        internal val WEI_PER_ETHER = BigDecimal.valueOf(1e18)
    }
}