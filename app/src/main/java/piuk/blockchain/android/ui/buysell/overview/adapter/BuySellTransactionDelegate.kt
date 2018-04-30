package piuk.blockchain.android.ui.buysell.overview.adapter

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.item_balance.view.*
import piuk.blockchain.android.R
import piuk.blockchain.android.ui.adapters.AdapterDelegate
import piuk.blockchain.android.ui.buysell.overview.BuySellDisplayable
import piuk.blockchain.android.ui.buysell.overview.BuySellTransaction
import piuk.blockchain.android.util.DateUtil
import piuk.blockchain.androidbuysell.models.coinify.TradeState
import piuk.blockchain.androidcoreui.utils.extensions.getContext
import piuk.blockchain.androidcoreui.utils.extensions.gone
import piuk.blockchain.androidcoreui.utils.extensions.inflate

internal class BuySellTransactionDelegate : AdapterDelegate<BuySellDisplayable> {

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

        holder.bind(items[position] as BuySellTransaction)
    }

    private class BuySellTransactionViewHolder internal constructor(
            itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        private val date = itemView.date
        private val direction = itemView.direction
        private val result = itemView.result

        init {
            itemView.watch_only.gone()
            itemView.double_spend_warning.gone()
            itemView.tx_note.gone()
        }

        internal fun bind(buySellTransaction: BuySellTransaction) {

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