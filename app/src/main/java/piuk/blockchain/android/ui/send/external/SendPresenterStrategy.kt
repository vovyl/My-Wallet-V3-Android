package piuk.blockchain.android.ui.send.external

import android.content.Intent
import android.text.Editable
import android.widget.EditText
import info.blockchain.balance.CryptoCurrency
import info.blockchain.wallet.api.data.FeeOptions
import piuk.blockchain.android.ui.send.DisplayFeeOptions
import piuk.blockchain.androidcoreui.ui.base.BasePresenter
import java.text.DecimalFormatSymbols

abstract class SendPresenter<View : piuk.blockchain.androidcoreui.ui.base.View> : SendPresenterStrategy<View>() {

    abstract fun getFeeOptionsForDropDown(): List<DisplayFeeOptions>

    abstract fun updateFiatTextField(editable: Editable, amountCrypto: EditText)

    abstract fun updateCryptoTextField(editable: Editable, amountFiat: EditText)
}

abstract class SendPresenterStrategy<View : piuk.blockchain.androidcoreui.ui.base.View> : BasePresenter<View>() {

    abstract fun onContinueClicked()

    abstract fun onSpendMaxClicked()

    abstract fun onBroadcastReceived()

    abstract fun onResume()

    abstract fun onCurrencySelected(currency: CryptoCurrency)

    abstract fun handleURIScan(untrimmedscanData: String?)

    abstract fun handlePrivxScan(scanData: String?)

    abstract fun clearReceivingObject()

    abstract fun selectSendingAccount(data: Intent?, currency: CryptoCurrency)

    abstract fun selectReceivingAccount(data: Intent?, currency: CryptoCurrency)

    abstract fun selectDefaultOrFirstFundedSendingAccount()

    abstract fun submitPayment()

    abstract fun shouldShowAdvancedFeeWarning(): Boolean

    abstract fun onCryptoTextChange(cryptoText: String)

    abstract fun spendFromWatchOnlyBIP38(pw: String, scanData: String)

    abstract fun setWarnWatchOnlySpend(warn: Boolean)

    abstract fun onNoSecondPassword()

    abstract fun onSecondPasswordValidated(validateSecondPassword: String)

    abstract fun disableAdvancedFeeWarning()

    abstract fun getBitcoinFeeOptions(): FeeOptions?

    fun getDefaultDecimalSeparator() = DecimalFormatSymbols.getInstance().decimalSeparator.toString()
}
