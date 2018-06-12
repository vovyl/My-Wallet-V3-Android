package piuk.blockchain.android.ui.buysell.createorder.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import piuk.blockchain.androidbuysell.models.coinify.Quote

@Parcelize
data class ConfirmationDisplay(
        val quoteId: Int,
        val currencyToSend: String,
        val currencyToReceive: String,
        val amountToSend: Double,
        val amountToReceive: Double,
        val expiryTime: String,
        val orderFee: String,
        val paymentFee: String,
        val totalAmountToReceiveFormatted: String,
        val totalCostFormatted: String,
        val originalQuote: ParcelableQuote
) : Parcelable

@Parcelize
data class ParcelableQuote(
        val id: Int?,
        val baseCurrency: String,
        val quoteCurrency: String,
        val baseAmount: Double,
        val quoteAmount: Double,
        val issueTime: String,
        val expiryTime: String
) : Parcelable {

    companion object {

        fun fromQuote(quote: Quote): ParcelableQuote = ParcelableQuote(
                id = quote.id,
                baseCurrency = quote.baseCurrency,
                quoteCurrency = quote.quoteCurrency,
                baseAmount = quote.baseAmount,
                quoteAmount = quote.quoteAmount,
                issueTime = quote.issueTime,
                expiryTime = quote.expiryTime
        )

    }
}