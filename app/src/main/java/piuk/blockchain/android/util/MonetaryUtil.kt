package piuk.blockchain.android.util

import piuk.blockchain.android.util.annotations.Mockable
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*

/**
 * A class for various currency related operations, mostly converting between types and formats.
 */
@Mockable
class MonetaryUtil() {

    private lateinit var btcFormat: DecimalFormat
    private lateinit var ethFormat: DecimalFormat
    private lateinit var fiatFormat: DecimalFormat

    init {
        val defaultLocale = Locale.getDefault()

        fiatFormat = (NumberFormat.getInstance(defaultLocale) as DecimalFormat).apply {
            maximumFractionDigits = 2
            minimumFractionDigits = 2
        }

        btcFormat = (NumberFormat.getInstance(defaultLocale) as DecimalFormat).apply {
            minimumFractionDigits = 1
            maximumFractionDigits = 8
        }

        ethFormat = (NumberFormat.getInstance(defaultLocale) as DecimalFormat).apply {
            maximumFractionDigits = 18
            minimumFractionDigits = 2
        }
    }

    /**
     * Returns the current BTC format as a [NumberFormat] object.
     *
     * @return A [NumberFormat] object with the correct decimal fractions for the current BTC format
     */
    fun getBtcFormat() = btcFormat

    /**
     * Returns the Fiat format as a [NumberFormat] object for a given currency code.
     *
     * @param fiat The currency code (ie USD) for the format you wish to return
     * @return A [NumberFormat] object with the correct decimal fractions for the chosen Fiat format
     * @see ExchangeRateFactory.getCurrencyLabels
     */
    fun getFiatFormat(fiat: String) = fiatFormat.apply { currency = Currency.getInstance(fiat) }

    /**
     * Returns the current BTC format as a [NumberFormat] object.
     *
     * @return A [NumberFormat] object with the correct decimal fractions for the current BTC format
     */
    fun getEthFormat() = ethFormat

    /**
     * Accepts a [Long] value in Satoshis and returns the display amount as a [String] based on the
     * chosen [unit] type. Compared to [getDisplayAmountWithFormatting], this method does not return
     * Strings formatted to a particular region, and therefore don't feature delimiters (ie returns
     * "1000.0", not "1,000.0).
     *
     * eg. 10_000 Satoshi -> "0.0001" when unit == UNIT_BTC
     *
     * @param value The amount to be formatted in Satoshis
     * @return An amount formatted as a [String]
     */
    fun getDisplayAmount(value: Long): String = getBtcFormat().format(value / BTC_DEC)

    /**
     * Accepts a [Long] value in Satoshis and returns the display amount as a [String] based on the
     * chosen [unit] type. This method adds delimiters based on the [Locale].
     *
     * eg. 10_000_000_000 Satoshi -> "100,000.0" when unit == MILLI_BTC
     *
     * @param value The amount to be formatted in Satoshis
     * @return An amount formatted as a [String]
     */
    fun getDisplayAmountWithFormatting(value: Long): String {
        return getBtcFormat().format(value / BTC_DEC)
    }

    /**
     * Accepts a [Double] value in Satoshis and returns the display amount as a [String] based on the
     * chosen [unit] type. This method adds delimiters based on the [Locale].
     *
     * eg. 10_000_000_000.0 Satoshi -> "100,000.0" when unit == MILLI_BTC
     *
     * @param value The amount to be formatted in Satoshis
     * @return An amount formatted as a [String]
     */
    fun getDisplayAmountWithFormatting(value: Double): String {
        return getBtcFormat().format(value / BTC_DEC)
    }

    /**
     * Accepts a [Double] value in fiat currency and returns a [String] formatted to the region
     * with the correct currency symbol. For example, 1.2345 with country code "USD" and locale
     * [Locale.UK] would return "US$1.23".
     *
     * @param amount The amount of fiat currency to be formatted as a [Double]
     * @param currencyCode The 3-letter currency code, eg. "GBP"
     * @param locale The current device [Locale]
     * @return The formatted currency [String]
     */
    fun getFiatDisplayString(amount: Double, currencyCode: String, locale: Locale): String {
        val numberFormat = NumberFormat.getCurrencyInstance(locale)
        val decimalFormatSymbols = (numberFormat as DecimalFormat).decimalFormatSymbols
        numberFormat.decimalFormatSymbols = decimalFormatSymbols.apply {
            this.currencySymbol = getCurrencySymbol(currencyCode, locale)
        }
        return numberFormat.format(amount)
    }

    /**
     * Returns the symbol for the chosen currency, based on the passed currency code and the chosen
     * device [Locale].
     *
     * @param currencyCode The 3-letter currency code, eg. "GBP"
     * @param locale The current device [Locale]
     * @return The correct currency symbol (eg. "$")
     */
    fun getCurrencySymbol(currencyCode: String, locale: Locale): String =
            Currency.getInstance(currencyCode).getSymbol(locale)

    companion object {
        private const val BTC_DEC = 1e8
    }
}
