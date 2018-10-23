package piuk.blockchain.android.ui.send.external

import info.blockchain.balance.CryptoCurrency
import info.blockchain.balance.CryptoValue
import info.blockchain.balance.FiatValue

interface BaseSendView : piuk.blockchain.androidcoreui.ui.base.View {

    fun setSelectedCurrency(cryptoCurrency: CryptoCurrency)

    fun updateFiatCurrency(currency: String)

    fun updateReceivingHintAndAccountDropDowns(currency: CryptoCurrency, listSize: Int)

    fun updateCryptoAmountWithoutTriggeringListener(cryptoValue: CryptoValue)

    fun updateFiatAmountWithoutTriggeringListener(fiatValue: FiatValue)
}
