package piuk.blockchain.android.ui.buysell.payment

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.rxkotlin.subscribeBy
import piuk.blockchain.android.data.payments.SendDataManager
import piuk.blockchain.android.util.extensions.addToCompositeDisposable
import piuk.blockchain.androidbuysell.datamanagers.CoinifyDataManager
import piuk.blockchain.androidbuysell.services.ExchangeService
import piuk.blockchain.androidcore.data.currency.CurrencyFormatManager
import piuk.blockchain.androidcore.utils.extensions.applySchedulers
import piuk.blockchain.androidcoreui.ui.base.BasePresenter
import javax.inject.Inject


class BuySellBuildOrderPresenter @Inject constructor(
        private val coinifyDataManager: CoinifyDataManager,
        private val sendDataManager: SendDataManager,
        private val exchangeService: ExchangeService,
        private val currencyFormatManager: CurrencyFormatManager
) : BasePresenter<BuySellBuildOrderView>() {

    private val tokenObservable: Observable<String>
        get() = exchangeService.getExchangeMetaData()
                .addToCompositeDisposable(this)
                .applySchedulers()
                .map { it.coinify!!.token }

    override fun onViewReady() {
        // Get quote for value of 1 BTC for UI using default currency
        tokenObservable
                .doOnSubscribe { view.renderQuoteStatus(QuoteStatus.Loading) }
                .singleOrError()
                .flatMapCompletable { token ->
                    coinifyDataManager.getTrader(token)
                            .flatMapCompletable { trader ->
                                getQuote(token, -1.0, trader.defaultCurrency)
                                        .andThen(loadCurrencies(token, trader.defaultCurrency))
                            }
                }
                .subscribeBy(
                        onComplete = {

                        },
                        onError = { }
                )
    }

    // TODO: Here we grab the first payment method - what if it's not applicable?  
    private fun loadCurrencies(token: String, userCurrency: String): Completable =
            coinifyDataManager.getPaymentMethods(token)
                    .toList()
                    .doOnSuccess {
                        val currencies = it[0].inCurrencies.toMutableList()
                        if (currencies.contains(userCurrency)) {
                            val index = currencies.indexOf(userCurrency)
                            currencies.removeAt(index)
                            currencies.add(0, userCurrency)
                            view.setupSpinner(currencies)
                        } else {
                            view.setupSpinner(currencies)
                        }
                    }
                    .doOnError {
                        // TODO: Most likely want to quit the page here
                    }
                    .ignoreElement()

    private fun getQuote(token: String, amount: Double, currency: String): Completable =
            coinifyDataManager.getQuote(
                    token,
                    amount,
                    "BTC",
                    currency
            ).doOnSuccess {
                val valueWithSymbol =
                        currencyFormatManager.getFormattedFiatValueWithSymbol(
                                it.quoteAmount,
                                it.quoteCurrency,
                                view.locale
                        )

                view.renderQuoteStatus(QuoteStatus.Data("@ $valueWithSymbol"))

            }.doOnError { view.renderQuoteStatus(QuoteStatus.Failed) }
                    .ignoreElement()

    internal fun onCurrencySelected(currency: String) {
        tokenObservable
                .doOnSubscribe { view.renderQuoteStatus(QuoteStatus.Loading) }
                .singleOrError()
                .flatMap {
                    coinifyDataManager.getQuote(
                            it,
                            -1.0,
                            "BTC",
                            currency
                    )
                }
                .subscribeBy(
                        onSuccess = {
                            val valueWithSymbol =
                                    currencyFormatManager.getFormattedFiatValueWithSymbol(
                                            it.quoteAmount,
                                            it.quoteCurrency,
                                            view.locale
                                    )

                            view.renderQuoteStatus(QuoteStatus.Data("@ $valueWithSymbol"))
                        },
                        onError = { view.renderQuoteStatus(QuoteStatus.Failed) }
                )
    }

    sealed class QuoteStatus {

        object Loading : QuoteStatus()
        data class Data(val formattedQuote: String) : QuoteStatus()
        object Failed : QuoteStatus()

    }
}