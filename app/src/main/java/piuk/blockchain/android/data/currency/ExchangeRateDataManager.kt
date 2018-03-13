package piuk.blockchain.android.data.currency

import io.reactivex.Observable
import piuk.blockchain.android.data.currency.datastore.ExchangeRateDataStore
import piuk.blockchain.android.data.rxjava.RxBus
import piuk.blockchain.android.data.rxjava.RxPinning
import piuk.blockchain.android.data.rxjava.RxUtil
import piuk.blockchain.android.util.annotations.Mockable
import java.math.BigDecimal
import java.math.BigInteger
import java.util.*

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

    /**
     * Returns the symbol for the chosen currency, based on the passed currency code and the chosen
     * device [Locale].
     *
     * @param currencyCode The 3-letter currency code, eg. "GBP"
     * @param locale The current device [Locale]
     * @return The correct currency symbol (eg. "$")
     */
    fun getCurrencySymbol(currencyCode: String, locale: Locale): String =
            Currency.getInstance(currencyCode).getSymbol(locale)

    fun getCurrencyLabels() = exchangeRateDataStore.getCurrencyLabels()

    fun getBtcHistoricPrice(
            satoshis: Long,
            currency: String,
            timeInSeconds: Long
    ): Observable<Double> = rxPinning.call<Double> {
        exchangeRateDataStore.getBtcHistoricPrice(satoshis, currency, timeInSeconds)
    }

    fun getBchHistoricPrice(
            satoshis: Long,
            currency: String,
            timeInSeconds: Long
    ): Observable<Double> = rxPinning.call<Double> {
        exchangeRateDataStore.getBchHistoricPrice(satoshis, currency, timeInSeconds)
    }

    fun getEthHistoricPrice(
            wei: BigInteger,
            currency: String,
            timeInSeconds: Long
    ): Observable<Double> = rxPinning.call<Double> {
        exchangeRateDataStore.getEthHistoricPrice(wei, currency, timeInSeconds)
    }
    companion object {
        internal val SATOSHIS_PER_BITCOIN = BigDecimal.valueOf(100_000_000L)
        internal val WEI_PER_ETHER = BigDecimal.valueOf(1e18)
    }
}