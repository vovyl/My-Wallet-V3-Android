package piuk.blockchain.android.ui.buysell.overview

import android.support.annotation.StringRes
import io.reactivex.Observable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.subjects.PublishSubject
import piuk.blockchain.android.R
import piuk.blockchain.android.ui.buysell.overview.models.BuySellButtons
import piuk.blockchain.android.ui.buysell.overview.models.BuySellDisplayable
import piuk.blockchain.android.ui.buysell.overview.models.BuySellTransaction
import piuk.blockchain.android.ui.buysell.overview.models.EmptyTransactionList
import piuk.blockchain.android.ui.buysell.overview.models.KycInProgress
import piuk.blockchain.android.util.extensions.addToCompositeDisposable
import piuk.blockchain.androidbuysell.datamanagers.CoinifyDataManager
import piuk.blockchain.androidbuysell.models.coinify.CoinifyTrade
import piuk.blockchain.androidbuysell.models.coinify.TradeState
import piuk.blockchain.androidbuysell.services.ExchangeService
import piuk.blockchain.androidbuysell.utils.fromIso8601
import piuk.blockchain.androidcore.data.currency.CurrencyFormatManager
import piuk.blockchain.androidcore.data.metadata.MetadataManager
import piuk.blockchain.androidcore.utils.extensions.applySchedulers
import piuk.blockchain.androidcoreui.ui.base.BasePresenter
import timber.log.Timber
import javax.inject.Inject

class CoinifyOverviewPresenter @Inject constructor(
        private val exchangeService: ExchangeService,
        private val coinifyDataManager: CoinifyDataManager,
        private val metadataManager: MetadataManager,
        private val formatManager: CurrencyFormatManager
) : BasePresenter<CoinifyOverviewView>() {

    // Display States
    private val buttons = BuySellButtons()
    private val kycInReview = KycInProgress()
    private val empty = EmptyTransactionList()
    // Display List
    private val displayList: MutableList<BuySellDisplayable> = mutableListOf(buttons)
    private val _displayListObservable = PublishSubject.create<BuySellDisplayable>()
    val displayListObservable: Observable<BuySellDisplayable>
        get() = _displayListObservable.hide()

    override fun onViewReady() {
        // TODO: Compare metadata trades with coinify trades; if order ID is missing, add to metadata
        renderTrades(emptyList())
        view.renderViewState(OverViewState.Loading)
        updateTransactionList()
        checkKycStatus()
    }

    private fun checkKycStatus() {
        exchangeService.getExchangeMetaData()
                .addToCompositeDisposable(this)
                .applySchedulers()
                .map { it.coinify!!.token }
                .flatMapSingle { coinifyDataManager.getKycReviews(it) }
                .subscribeBy(
                        onNext = {
                            for (kycResponse in it) {
                                if (kycResponse.state.isProcessing()) {
                                    displayList.add(0, kycInReview)
                                    view.renderViewState(OverViewState.Data(displayList.toList()))
                                    break
                                }
                            }
                        },
                        onError = { Timber.e(it) }
                )

    }

    internal fun updateTransactionList() {
        exchangeService.getExchangeMetaData()
                .addToCompositeDisposable(this)
                .applySchedulers()
                .map { it.coinify!!.token }
                .flatMap { coinifyDataManager.getTrades(it) }
                .map { mapTradeToDisplayObject(it) }
                .toList()
                .doOnError { Timber.e(it) }
                .subscribeBy(
                        onSuccess = { renderTrades(it) },
                        onError = {
                            view.renderViewState(OverViewState.Failure(R.string.buy_sell_overview_error_loading_transactions))
                        }
                )
    }

    private fun renderTrades(trades: List<BuySellTransaction>) {
        displayList.removeAll { it is BuySellTransaction }
        displayList.apply { addAll(trades) }
                .apply {
                    if (trades.isEmpty()) {
                        add(empty)
                    } else {
                        removeAll { it is EmptyTransactionList }
                    }
                }
        view.renderViewState(OverViewState.Data(displayList.toList()))
    }

    private fun mapTradeToDisplayObject(coinifyTrade: CoinifyTrade): BuySellTransaction {
        val displayString = if (coinifyTrade.isSellTransaction()) {
            "-${coinifyTrade.inAmount} ${coinifyTrade.inCurrency.capitalize()}"
        } else {
            if (coinifyTrade.state.isEndState() && !coinifyTrade.state.isFailureState()) {
                "+${coinifyTrade.outAmount} ${coinifyTrade.outCurrency.capitalize()}"
            } else {
                "+${coinifyTrade.outAmountExpected} ${coinifyTrade.outCurrency.capitalize()}"
            }
        }

        return BuySellTransaction(
                transactionId = coinifyTrade.id,
                time = coinifyTrade.createTime.fromIso8601()!!,
                displayAmount = displayString,
                tradeStateString = tradeStateToStringRes(coinifyTrade.state),
                tradeState = coinifyTrade.state,
                isSellTransaction = coinifyTrade.isSellTransaction()
        )
    }

    @StringRes
    private fun tradeStateToStringRes(state: TradeState): Int = when (state) {
        TradeState.AwaitingTransferIn -> R.string.buy_sell_state_awaiting_funds
        TradeState.Completed -> R.string.buy_sell_state_completed
        TradeState.Cancelled -> R.string.buy_sell_state_cancelled
        TradeState.Rejected -> R.string.buy_sell_state_rejected
        TradeState.Expired -> R.string.buy_sell_state_expired
        TradeState.Processing, TradeState.Reviewing -> R.string.buy_sell_state_processing
    }
}

sealed class OverViewState {

    object Loading : OverViewState()
    class Failure(@StringRes val message: Int) : OverViewState()
    class Data(val items: List<BuySellDisplayable>) : OverViewState()

}