package piuk.blockchain.android.ui.send.external

import com.blockchain.transactions.SendDetails
import info.blockchain.balance.AccountReference
import info.blockchain.balance.CryptoValue
import info.blockchain.balance.FiatValue

data class SendConfirmationDetails(
    val sendDetails: SendDetails,
    val fees: CryptoValue,
    val fiatAmount: FiatValue,
    val fiatFees: FiatValue
) {
    val from: AccountReference = sendDetails.from
    val to: String = sendDetails.toAddress
    val amount: CryptoValue = sendDetails.value

    val total = amount + fees
    val fiatTotal = fiatAmount + fiatFees
}
