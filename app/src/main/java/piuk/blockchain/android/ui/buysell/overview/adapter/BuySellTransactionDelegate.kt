package piuk.blockchain.android.ui.buysell.overview.adapter

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.item_balance.view.*
import piuk.blockchain.android.R
import piuk.blockchain.android.ui.adapters.AdapterDelegate
import piuk.blockchain.android.ui.buysell.overview.models.BuySellDisplayable
import piuk.blockchain.android.ui.buysell.overview.models.BuySellTransaction
import piuk.blockchain.android.util.DateUtil
import piuk.blockchain.androidbuysell.models.coinify.TradeState
import piuk.blockchain.androidcoreui.utils.extensions.getContext
import piuk.blockchain.androidcoreui.utils.extensions.gone
import piuk.blockchain.androidcoreui.utils.extensions.inflate

internal class BuySellTransactionDelegate(
        private val listener: CoinifyTxFeedListener
) : AdapterDelegate<BuySellDisplayable> {

    override fun isForViewType(items: List<BuySellDisplayable>, position: Int): Boolean =
            items[position] is BuySellTransaction

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder =
            BuySellTransactionViewHolder(parent.inflate(R.layout.item_balance))

    override fun onBindViewHolder(
            items: List<BuySellDisplayable>,
            position: Int,
            holder: RecyclerView.ViewHolder,
            payloads: List<*>
    ) {
        holder as BuySellTransactionViewHolder

        val displayable = items[position]
        holder.bind(displayable as BuySellTransaction, listener, displayable.transactionId)
    }

    private class BuySellTransactionViewHolder(
            itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        private val date = itemView.date
        private val direction = itemView.direction
        private val result = itemView.result
        private val root = itemView

        init {
            itemView.watch_only.gone()
            itemView.double_spend_warning.gone()
            itemView.tx_note.gone()
        }

        fun bind(
                buySellTransaction: BuySellTransaction,
                listener: CoinifyTxFeedListener,
                transactionId: Int
        ) {

            root.setOnClickListener { listener.onTransactionClicked(transactionId) }

            date.text = DateUtil(getContext()).formatted(buySellTransaction.time.time / 1000)
            result.text = buySellTransaction.outAmount

            when (buySellTransaction.tradeState) {
                TradeState.AwaitingTransferIn -> direction.text = "Awaiting transfer"
                TradeState.Processing -> direction.text = "Processing"
                TradeState.Reviewing -> direction.text = "Reviewing"
                TradeState.Completed -> direction.text = "Completed"
                TradeState.Cancelled -> direction.text = "Cancelled"
                TradeState.Rejected -> direction.text = "Rejected"
                TradeState.Expired -> direction.text = "Expired"
            }

        }

    }

}