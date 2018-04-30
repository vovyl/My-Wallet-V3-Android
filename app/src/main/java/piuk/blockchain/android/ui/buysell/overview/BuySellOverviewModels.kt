package piuk.blockchain.android.ui.buysell.overview

import piuk.blockchain.androidbuysell.models.coinify.TradeState
import java.util.*

/**
 * Marker interface for all RecyclerView classes
 */
interface BuySellDisplayable

/**
 * To display Buy/Sell Buttons in the RecyclerView
 */
class BuySellButtons : BuySellDisplayable

/**
 * Represents zero transactions in the RecyclerView
 */
class EmptyTransactionList : BuySellDisplayable

/**
 * Wrapper for buy/sell transactions
 */
data class BuySellTransaction(
        val time: Date,
        val inCurrency: String,
        val outCurrency: String,
        val inAmount: String,
        val outAmount: String,
        val tradeState: TradeState
) : BuySellDisplayable