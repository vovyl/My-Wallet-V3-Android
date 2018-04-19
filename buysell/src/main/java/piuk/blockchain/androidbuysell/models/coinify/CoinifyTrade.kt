package piuk.blockchain.androidbuysell.models.coinify

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.ToJson
import org.web3j.tx.Transfer

data class CoinifyTrade(
        /** Unique ID for this trade */
        val id: Int,
        /** Reference to the trader that created the trade */
        val traderId: Int,
        /** The current state of the trade */
        val state: TradeState,
        /** Currency (ISO 4217) denominating [inAmount] */
        val inCurrency: String,
        /** Currency (ISO 4217) denominating [outAmount] or [outAmountExpected] */
        val outCurrency: String,
        /** The amount of [inAmount] that this trade covers. Is always positive. */
        val inAmount: Double,
        /**
         * (Optional) The amount of outCurrency that this trade resulted in. Is always positive.
         * NOTE: This field is only defined if the state of the trade is completed. For all other
         * states, see the [outAmountExpected] field.
         */
        val outAmount: Double,
        /** The amount of outCurrency that this trade is expected to result in. Is always positive. */
        val outAmountExpected: Double,
        /** Object describing how Coinify will receive/have received the money to fund the trade. */
        val transferIn: Transfer,
        /** Object describing how Coinify will send/have sent the result of the trade back to the trader. */
        val transferOut: Transfer,
        /** (Optional). URL to a printable receipt for this trade. This field is present if [state] is complete. */
        val receiptUrl: String?,
        /**
         * (Optional) The time when the price quote underlying this trade expires. NOTE: This field
         * is only defined in the following states: awaiting_transfer_in, processing, reviewing.
         */
        val quoteExpireTime: String?,
        /** The time when the trade was last updated (ISO 8601). */
        val updateTime: String,
        /** Timestamp for when this trade was first created (ISO 8601). */
        val createTime: String
)

data class Transfer(
        /** Unique identifier for this transfer. */
        val id: Int,
        /** State of this transfer */
        val state: ReviewState,
        /** Currency (ISO 4217) that this transfer is/will be denominated in. */
        val currency: String,
        /** Amount that is/will be sent to this transfer. Denominated in [currency]. Is always positive. */
        val sendAmount: Double,
        /**
         * Amount that this transfer will result in. Denominated in [currency]. Is always positive
         * and equal to or smaller then [sendAmount]. The difference between the [sendAmount] and
         * the [receiveAmount] will be the fee of the transfer.
         */
        val receiveAmount: Double,
        /** Transfer medium. The medium transferring the currency. */
        val medium: Medium,
        /**
         * Reference to either Coinify's internal accounts or the user's account. Note at the
         * moment Coinify only have internal bank accounts.
         */
        val mediumReceiveAccountId: Int,
        /** Information relevant for this medium */
        val details: Details,
        /** The fee for this transfer */
        val fee: Double = sendAmount - receiveAmount
)

// Marker interface
interface Details

data class BlockchainDetails(
        /** Either the trader's or Coinify's bitcoin address */
        val account: String,
        /** The BTC transaction that sent out the BTC to the above address. Not present if unconfirmed */
        val tx: String?
) : Details

data class CardDetails(
        /**	String identifying the PSP. Current possible values are (‘isignthis’,'isignthis-staging’,'paylike’) */
        val provider: String,
        /** String identifying the PSP’s merchant. Only relevant when provider='paylike’ */
        val providerMerchantId: String,
        /**
         *  Id of the external payment. (For iSignThis, its the transaction id). The paymentId is
         *  used for the integration mode: embedded mode.
         */
        val paymentId: String,
        /**	Reference to the card payment. */
        val cardPaymentId: Int,
        /**
         * The return URL to which the user to be sent back after the payment has been created.
         * Can be provided when creating a trade. Only relevant when provider = 'isignthis’ or
         * provider = 'isignthis-staging’
         */
        val returnUrl: String,
        /**
         * Redirect URL to process the payment. Only relevant when provider = 'isignthis’ or
         * provider = 'isignthis-staging’. The redirectUrl is used for the integration mode:
         * redirect mode.
         */
        val redirectUrl: String
) : Details

data class BankDetails(
        /**
         * Text that the bank transfer must contain in order for Coinify to correctly register
         * what trade it concerns.
         */
        val referenceText: String,
        /** Object with additional information about the bank account. */
        val account: Account,
        /** Object with additional information about the bank. */
        val bank: Bank,
        /** Object with additional information about the bank account holder. */
        val holder: Holder,
        /** The time when the bank account was last updated (ISO 8601). */
        val updateTime: String,
        /** The time when the bank account was created (ISO 8601). */
        val createTime: String

) : Details

data class Account(
        /** Currency of the bank account */
        val currency: String,
        /** Type of the bank account, can be "danish", "sepa" or "international */
        // TODO: Not sure if this is super relevant to us so not parsed to sealed class/enum
        val type: String,
        /** For sepa and international its the SWIFT / BIC number and for danish accounts its the REG number. */
        val bic: String,
        /**
         * For sepa and international it’s the IBAN (International Bank Account Number). For
         * danish accounts, it’s the BBAN (Basic Bank Account Number).
         */
        val number: String
)

data class Bank(val address: Address)

data class Holder(
        val name: String,
        val address: Address
)

sealed class Medium {

    /**
     * Transfer on a public blockchain, such as Bitcoin. NOTE: The currency field of the transfer
     * object will determine which blockchain currency. For now we only support trading with bitcoin.
     */
    object Blockchain : Medium()

    /** Card payment */
    object Card : Medium()

    /**
     * Bank account payment. If transfer is incoming we sent our own bank details as the details
     * object. And if transfer is outgoing its the details of the traders bank account.
     */
    object Bank : Medium()

}

@Suppress("unused")
class MediumAdapter {

    @FromJson
    fun fromJson(input: String): Medium = when (input) {
        BANK -> Medium.Bank
        CARD -> Medium.Card
        BLOCKCHAIN -> Medium.Blockchain
        else -> throw JsonDataException("Unknown medium $input, unsupported data type")
    }

    @ToJson
    fun toJson(medium: Medium): String = when (medium) {
        Medium.Bank -> BANK
        Medium.Card -> CARD
        Medium.Blockchain -> BLOCKCHAIN
    }

    private companion object {
        private const val BANK = "bank"
        private const val CARD = "card"
        private const val BLOCKCHAIN = "blockchain"
    }
}

sealed class TradeState {
    // (Starting state) Trade was successfully created, and is waiting for trader’s payment
    object AwaitingTransferIn : TradeState()

    // Trade has received the trader’s payment, and we are processing the trade.
    object Processing : TradeState()

    // Trade is undergoing manual review
    object Reviewing : TradeState()

    // 	(Ending state) Trade completed successfully.
    object Completed : TradeState()

    // (Ending state) Trade cancelled.
    object Cancelled : TradeState()

    // (Ending state) Trade was rejected.
    object Rejected : TradeState()

    // (Ending state) Trade expired before it completed.
    object Expired : TradeState()

    fun isEndState(): Boolean = (this === Completed
            || this === Cancelled
            || this === Rejected
            || this === Expired)

}

@Suppress("unused")
class TradeStateAdapter {

    @FromJson
    fun fromJson(input: String): TradeState = when (input) {
        AWAITING_TRANSFER_IN -> TradeState.AwaitingTransferIn
        PROCESSING -> TradeState.Processing
        REVIEWING -> TradeState.Reviewing
        COMPLETED -> TradeState.Completed
        CANCELLED -> TradeState.Cancelled
        REJECTED -> TradeState.Rejected
        EXPIRED -> TradeState.Expired
        else -> throw JsonDataException("Unknown trade state $input, unsupported data type")
    }

    @ToJson
    fun toJson(tradeState: TradeState): String = when (tradeState) {
        TradeState.AwaitingTransferIn -> AWAITING_TRANSFER_IN
        TradeState.Processing -> PROCESSING
        TradeState.Reviewing -> REVIEWING
        TradeState.Completed -> COMPLETED
        TradeState.Cancelled -> CANCELLED
        TradeState.Rejected -> REJECTED
        TradeState.Expired -> EXPIRED
    }

    private companion object {
        private const val AWAITING_TRANSFER_IN = "awaiting_transfer_in"
        private const val PROCESSING = "processing"
        private const val REVIEWING = "reviewing"
        private const val COMPLETED = "completed"
        private const val CANCELLED = "cancelled"
        private const val REJECTED = "rejected"
        private const val EXPIRED = "expired"
    }
}
