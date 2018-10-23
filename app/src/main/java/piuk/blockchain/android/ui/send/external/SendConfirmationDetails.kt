package piuk.blockchain.android.ui.send.external

import info.blockchain.balance.AccountReference
import info.blockchain.balance.CryptoValue
import info.blockchain.balance.FiatValue

data class SendConfirmationDetails(
    val from: AccountReference,
    val to: String,
    val amount: CryptoValue,
    val fees: CryptoValue,
    val fiatAmount: FiatValue,
    val fiatFees: FiatValue
) {
    val total = amount + fees
    val fiatTotal = fiatAmount + fiatFees
}
