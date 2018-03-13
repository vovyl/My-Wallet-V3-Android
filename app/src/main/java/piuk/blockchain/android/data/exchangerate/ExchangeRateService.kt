package piuk.blockchain.android.data.exchangerate

import info.blockchain.wallet.prices.PriceApi
import io.reactivex.Observable
import piuk.blockchain.android.data.currency.CryptoCurrencies
import piuk.blockchain.android.data.rxjava.RxUtil
import piuk.blockchain.android.util.annotations.WebRequest

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

    @WebRequest
    fun getBtcHistoricPrice(
            currency: String,
            timeInSeconds: Long
    ): Observable<Double> =
            priceApi.getHistoricPrice(CryptoCurrencies.BTC.symbol, currency, timeInSeconds)
                    .compose(RxUtil.applySchedulersToObservable())

    @WebRequest
    fun getEthHistoricPrice(
            currency: String,
            timeInSeconds: Long
    ): Observable<Double> =
            priceApi.getHistoricPrice(CryptoCurrencies.ETHER.symbol, currency, timeInSeconds)
                    .compose(RxUtil.applySchedulersToObservable())

    @WebRequest
    fun getBchHistoricPrice(
            currency: String,
            timeInSeconds: Long
    ): Observable<Double> =
            priceApi.getHistoricPrice(CryptoCurrencies.BCH.symbol, currency, timeInSeconds)
                    .compose(RxUtil.applySchedulersToObservable())
}