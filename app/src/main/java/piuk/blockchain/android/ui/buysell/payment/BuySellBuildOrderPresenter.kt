package piuk.blockchain.android.ui.buysell.payment

import io.reactivex.Single
import piuk.blockchain.android.data.payments.SendDataManager
import piuk.blockchain.android.util.extensions.addToCompositeDisposable
import piuk.blockchain.androidbuysell.datamanagers.CoinifyDataManager
import piuk.blockchain.androidbuysell.models.coinify.Medium
import piuk.blockchain.androidbuysell.models.coinify.PaymentMethod
import piuk.blockchain.androidbuysell.models.coinify.Quote
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

    private val tokenSingle: Single<String>
        get() = exchangeService.getExchangeMetaData()
                .addToCompositeDisposable(this)
                .applySchedulers()
                .singleOrError()
                .doOnError {
                    // TODO: Most likely fatal, error fetching token
                }
                .map { it.coinify!!.token }

    override fun onViewReady() {
        // Get quote for value of 1 BTC for UI using default currency
        tokenSingle
                .doOnSubscribe { view.renderSpinnerStatus(SpinnerStatus.Loading) }
                .flatMapCompletable { token ->
                    coinifyDataManager.getTrader(token)
                            .flatMapCompletable { trader ->
                                // TODO: Render buy limits
                                // This requires trader info + bitcoin limits (for sell only)
                                getQuote(token, -1.0, trader.defaultCurrency)
                                        .ignoreElement()
                                        .andThen(
                                                loadCurrencies(token, trader.defaultCurrency)
                                                        .ignoreElement()
                                        )
                            }
                }
                .subscribe()
    }

    fun onCurrencySelected(currency: String) {
        tokenSingle
                .doOnSubscribe { view.renderQuoteStatus(QuoteStatus.Loading) }
                .flatMap { getQuote(it, -1.0, currency) }
                .subscribe()
    }

    // TODO: Here we grab the first payment method - what if it's not applicable?
    // To be fair I'm certain that web don't do this either, as they can't know the payment
    // medium in advance
    private fun loadCurrencies(token: String, userCurrency: String): Single<PaymentMethod> =
            coinifyDataManager.getPaymentMethods(token)
                    .filter { it.inMedium == Medium.Blockchain }
                    .firstOrError()
                    .doOnSuccess {
                        val currencies = it.inCurrencies.toMutableList()
                        if (currencies.contains(userCurrency)) {
                            val index = currencies.indexOf(userCurrency)
                            currencies.removeAt(index)
                            currencies.add(0, userCurrency)
                        }
                        view.renderSpinnerStatus(SpinnerStatus.Data(currencies))
                    }
                    .doOnError {
                        view.renderSpinnerStatus(SpinnerStatus.Failure)
                    }

    private fun getQuote(token: String, amount: Double, currency: String): Single<Quote> =
            coinifyDataManager.getQuote(token, amount, "BTC", currency)
                    .doOnSuccess {
                        val valueWithSymbol =
                                currencyFormatManager.getFormattedFiatValueWithSymbol(
                                        it.quoteAmount,
                                        it.quoteCurrency,
                                        view.locale
                                )

                        view.renderQuoteStatus(QuoteStatus.Data("@ $valueWithSymbol"))

                    }
                    .doOnError { view.renderQuoteStatus(QuoteStatus.Failed) }

    sealed class QuoteStatus {

        object Loading : QuoteStatus()
        data class Data(val formattedQuote: String) : QuoteStatus()
        object Failed : QuoteStatus()

    }

    sealed class SpinnerStatus {

        object Loading : SpinnerStatus()
        data class Data(val currencies: List<String>) : SpinnerStatus()
        object Failure : SpinnerStatus()

    }
}