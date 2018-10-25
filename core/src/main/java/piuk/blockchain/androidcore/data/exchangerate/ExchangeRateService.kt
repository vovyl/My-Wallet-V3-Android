package piuk.blockchain.androidcore.data.exchangerate

import info.blockchain.balance.CryptoCurrency
import info.blockchain.wallet.prices.PriceApi
import info.blockchain.wallet.prices.data.PriceDatum
import io.reactivex.Single

class ExchangeRateService(private val priceApi: PriceApi) {

    fun getExchangeRateMap(cryptoCurrency: CryptoCurrency): Single<Map<String, PriceDatum>> =
        priceApi.getPriceIndexes(cryptoCurrency.symbol)

    fun getHistoricPrice(
        cryptoCurrency: CryptoCurrency,
        currency: String,
        timeInSeconds: Long
    ): Single<Double> =
        priceApi.getHistoricPrice(cryptoCurrency.symbol, currency, timeInSeconds)
}