package piuk.blockchain.android.ui.buysell.overview

/**
 * Marker interface for all RecyclerView classes
 */
interface BuySellDisplayable

/**
 * To display Buy/Sell Buttons in the RecyclerView
 */
class BuySellButtons: BuySellDisplayable

/**
 * Represents zero transactions in the RecyclerView
 */
class EmptyTransactionList: BuySellDisplayable