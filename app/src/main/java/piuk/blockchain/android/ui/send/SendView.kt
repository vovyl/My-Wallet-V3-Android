package piuk.blockchain.android.ui.send

import android.support.annotation.ColorRes
import android.support.annotation.Nullable
import android.support.annotation.StringRes
import android.support.design.widget.Snackbar
import piuk.blockchain.android.ui.account.PaymentConfirmationDetails
import info.blockchain.balance.CryptoCurrency
import info.blockchain.balance.CryptoValue
import info.blockchain.balance.FiatValue
import piuk.blockchain.android.R
import piuk.blockchain.android.ui.send.external.SendConfirmationDetails
import piuk.blockchain.android.ui.send.external.BaseSendView
import java.util.Locale

interface SendView : BaseSendView {

    val locale: Locale

    // Update field
    fun updateSendingAddress(label: String)

    fun updateCryptoAmount(cryptoValue: CryptoValue)

    fun updateFiatAmount(fiatValue: FiatValue)

    fun updateWarning(message: String)

    fun updateMaxAvailable(maxAmount: String)

    fun updateMaxAvailable(maxAmount: CryptoValue, min: CryptoValue)

    fun updateMaxAvailableColor(@ColorRes color: Int)

    fun updateReceivingAddress(address: String)

    fun updateFeeAmount(fee: String)

    // Set property
    fun setCryptoMaxLength(length: Int)

    fun setFeePrioritySelection(index: Int)

    fun clearWarning()

    // Hide / Show
    fun showMaxAvailable()

    fun hideMaxAvailable()

    fun showFeePriority()

    fun hideFeePriority()

    // Enable / Disable
    fun enableFeeDropdown()

    fun disableFeeDropdown()

    fun setSendButtonEnabled(enabled: Boolean)

    fun disableInput()

    fun enableInput()

    // Fetch value
    fun getCustomFeeValue(): Long

    fun getClipboardContents(): String?

    fun getReceivingAddress(): String?

    fun getFeePriority(): Int

    // Prompts
    fun showSnackbar(@StringRes message: Int, duration: Int)

    fun showSnackbar(message: String, @Nullable extraInfo: String?, duration: Int)

    fun showEthContractSnackbar()

    fun showBIP38PassphrasePrompt(scanData: String)

    fun showWatchOnlyWarning(address: String)

    fun showProgressDialog(@StringRes title: Int)

    fun showSpendFromWatchOnlyWarning(address: String)

    fun showSecondPasswordDialog()

    fun showPaymentDetails(
        confirmationDetails: PaymentConfirmationDetails,
        note: String?,
        allowFeeChange: Boolean
    )

    fun showPaymentDetails(confirmationDetails: SendConfirmationDetails)

    fun showLargeTransactionWarning()

    fun showTransactionSuccess(cryptoCurrency: CryptoCurrency)

    fun showTransactionFailed() = showSnackbar(R.string.transaction_failed, Snackbar.LENGTH_LONG)

    fun showTransactionFailed(message: String) = showSnackbar(message, null, Snackbar.LENGTH_LONG)

    fun dismissProgressDialog()

    fun dismissConfirmationDialog()

    fun finishPage()

    fun hideCurrencyHeader()

    fun showMinBalanceLearnMore()
}

internal fun SendConfirmationDetails.toPaymentConfirmationDetails(): PaymentConfirmationDetails {
    return PaymentConfirmationDetails().also {
        it.fromLabel = from.label
        it.toLabel = to

        it.cryptoUnit = amount.symbol()
        it.cryptoAmount = amount.toStringWithoutSymbol()
        it.cryptoFee = fees.toStringWithoutSymbol()
        it.cryptoTotal = total.toStringWithoutSymbol()

        it.fiatUnit = fiatAmount.currencyCode
        it.fiatSymbol = fiatAmount.symbol()
        it.fiatAmount = fiatAmount.toStringWithoutSymbol()
        it.fiatFee = fiatFees.toStringWithoutSymbol()
        it.fiatTotal = fiatTotal.toStringWithoutSymbol()
    }
}
