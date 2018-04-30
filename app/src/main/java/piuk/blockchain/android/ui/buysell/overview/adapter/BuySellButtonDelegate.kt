package piuk.blockchain.android.ui.buysell.overview.adapter

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.item_buy_sell_buttons.view.*
import piuk.blockchain.android.R
import piuk.blockchain.android.ui.adapters.AdapterDelegate
import piuk.blockchain.android.ui.buysell.overview.BuySellButtons
import piuk.blockchain.android.ui.buysell.overview.BuySellDisplayable
import piuk.blockchain.androidcoreui.utils.extensions.inflate

class BuySellButtonDelegate : AdapterDelegate<BuySellDisplayable> {

    override fun isForViewType(items: List<BuySellDisplayable>, position: Int): Boolean=
            items[position] is BuySellButtons

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        val binding = parent.inflate(R.layout.item_buy_sell_buttons, false)
        return BuySellButtonViewHolder(binding)
    }

    override fun onBindViewHolder(
            items: List<BuySellDisplayable>,
            position: Int,
            holder: RecyclerView.ViewHolder,
            payloads: List<*>
    ) {
        holder as BuySellButtonViewHolder
        holder.bind()
    }

    private class BuySellButtonViewHolder internal constructor(
            itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        private val buy = itemView.image_view_buy_container
        private val sell = itemView.image_view_sell_container

        internal fun bind() {
            buy.setOnClickListener {  }
            sell.setOnClickListener {  }
        }

    }

}