package piuk.blockchain.android.ui.charts.models

import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

@Suppress("DataClassPrivateConstructor")
data class ArbitraryPrecisionFiatValue private constructor(
    val currencyCode: String,
    val value: BigDecimal
) {

    companion object {

        fun fromMajor(currencyCode: String, value: BigDecimal) =
            ArbitraryPrecisionFiatValue(currencyCode, value)
    }
}

fun ArbitraryPrecisionFiatValue.toStringWithSymbol(locale: Locale = Locale.getDefault()): String {
    val currencyInstance = Currency.getInstance(currencyCode)
    return (NumberFormat.getCurrencyInstance(locale) as DecimalFormat)
        .apply {
            decimalFormatSymbols =
                decimalFormatSymbols.apply {
                    currency = currencyInstance
                }
            maximumFractionDigits = value.scale()
            minimumFractionDigits = currencyInstance.defaultFractionDigits
        }
        .format(value)
        .trim()
}