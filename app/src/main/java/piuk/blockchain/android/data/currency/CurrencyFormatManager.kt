package piuk.blockchain.android.data.currency

import org.web3j.utils.Convert
import piuk.blockchain.android.data.exchangerate.ExchangeRateDataManager
import piuk.blockchain.android.util.PrefsUtil
import piuk.blockchain.android.util.annotations.Mockable
import java.math.BigDecimal
import java.math.BigInteger
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.ParseException
import java.util.*

@Mockable
class CurrencyFormatManager(val currencyState: CurrencyState,
                            val exchangeRateDataManager: ExchangeRateDataManager,
                            val prefsUtil: PrefsUtil,
                            val locale: Locale) {

    private lateinit var btcFormat: DecimalFormat
    private lateinit var ethFormat: DecimalFormat
    private lateinit var fiatFormat: DecimalFormat

    private val btcUnit = CryptoCurrencies.BTC.name
    private val bchUnit = CryptoCurrencies.BCH.name
    private val ethUnit = CryptoCurrencies.ETHER.name

    val maxBtcDecimalLength = 8
    val maxEthDecimalLength = 18

    init {
        val defaultLocale = Locale.getDefault()

        fiatFormat = (NumberFormat.getInstance(defaultLocale) as DecimalFormat).apply {
            maximumFractionDigits = 2
            minimumFractionDigits = 2
        }

        btcFormat = (NumberFormat.getInstance(defaultLocale) as DecimalFormat).apply {
            minimumFractionDigits = 1
            maximumFractionDigits = maxBtcDecimalLength
        }

        ethFormat = (NumberFormat.getInstance(defaultLocale) as DecimalFormat).apply {
            maximumFractionDigits = maxEthDecimalLength
            minimumFractionDigits = 2
        }
    }

    //region Current selected crypto currency state methods
    /**
     * Returns the maximum decimals allowed for current crypto currency state. Useful to apply max decimal length
     * on text fields.
     *
     * @return decimal length.
     */
    fun getCryptoMaxDecimalLength() =
            when (currencyState.cryptoCurrency) {
                CryptoCurrencies.BTC -> maxBtcDecimalLength
                CryptoCurrencies.ETHER -> maxEthDecimalLength
                CryptoCurrencies.BCH -> maxBtcDecimalLength
                else -> throw IllegalArgumentException(currencyState.cryptoCurrency.toString() + " not supported.")
            }

    /**
     * Crypto unit based on current crypto currency state.
     *
     * @return BTC, BCH or ETH.
     */
    fun getCryptoUnit() =
            when (currencyState.cryptoCurrency) {
                CryptoCurrencies.BTC -> btcUnit
                CryptoCurrencies.ETHER -> ethUnit
                CryptoCurrencies.BCH -> bchUnit
                else -> throw IllegalArgumentException(currencyState.cryptoCurrency.toString() + " not supported.")
            }

    /**
     * Accepts a [Long] value in Satoshis/Wei and returns the display amount as a [String] based on the
     * chosen [unit] type.
     *
     * eg. 10_000 Satoshi -> "0.0001" when unit == UNIT_BTC
     *
     * @param value The amount to be formatted in Satoshis
     * @return An amount formatted as a [String]
     */
    fun getFormattedCrypto(amount: BigDecimal) = getCryptoDecimalFormat().format(amount.toLong())

    fun getFormattedCrypto(amount: Long) = getCryptoDecimalFormat().format(amount)

    private fun getCryptoDecimalFormat() =
            when (currencyState.cryptoCurrency) {
                CryptoCurrencies.BTC -> btcFormat
                CryptoCurrencies.ETHER -> ethFormat
                CryptoCurrencies.BCH -> btcFormat
                else -> throw IllegalArgumentException(currencyState.cryptoCurrency.toString() + " not supported.")
            }

    /**
     * @return Last known exchange rate based on current crypto currency state.
     */
    fun getLastPrice(): Double {
        when (currencyState.cryptoCurrency) {
            CryptoCurrencies.BTC -> return exchangeRateDataManager.getLastBtcPrice(getFiatUnit())
            CryptoCurrencies.ETHER -> return exchangeRateDataManager.getLastEthPrice(getFiatUnit())
            CryptoCurrencies.BCH -> return exchangeRateDataManager.getLastBchPrice(getFiatUnit())
            else -> throw IllegalArgumentException(currencyState.cryptoCurrency.toString() + " not supported.")
        }
    }
    //endregion

    //region Formatted displayable strings - Current currency based
    /**
     * @return Formatted String of fiat or crypto amount with currency unit based on current currencyState.
     */
    fun getDisplayAmountWithUnit(amount: BigInteger): String {
        return if (currencyState.isDisplayingCryptoCurrency) {
            getDisplayCryptoWithUnit(amount)
        } else {
            getDisplayFiatWithUnit(amount)
        }
    }

    /**
     * @return Formatted String of crypto amount with currency unit based on current currencyState
     */
    fun getDisplayCryptoWithUnit(amount: BigInteger): String {
        return "${getDisplayCrypto(amount)} ${getCryptoUnit()}"
    }

    /**
     * @return Formatted String of fiat amount with currency unit.
     */
    fun getDisplayFiatWithUnit(amount: BigInteger): String {
        return "${getDisplayFiatFromCrypto(amount)} ${getFiatUnit()}"
    }

    /**
     * @return Formatted String of crypto amount based on current currencyState
     */
    fun getDisplayCrypto(amount: BigInteger): String {
        val amountFormatted = getCryptoDecimalFormat().format(Math.max(amount.toDouble(), 0.0) / 1e8)
        return "$amountFormatted"
    }

    /**
     * @return Formatted String of fiat amount.
     */
    fun getDisplayFiatFromCrypto(amount: BigInteger): String {
        val fiatBalance = getLastPrice() * (Math.max(amount.toDouble(), 0.0) / 1e8)
        val fiatBalanceFormatted = fiatFormat.format(getFiatUnit()).format(fiatBalance)

        return "$fiatBalanceFormatted"
    }

    fun getDisplayFiatFromCrypto(amount: BigDecimal): String {
        return getDisplayFiatFromCrypto(amount.toBigInteger())
    }

    /**
     * @return Formatted String of fiat amount from crypto currency amount.
     */
    fun getDisplayFiatFromCryptoString(cryptoAmountText: String): String {
        val cryptoAmount = cryptoAmountText.toSafeDouble(locale)
        val fiatAmount = getLastPrice() * cryptoAmount

        return fiatFormat.format(fiatAmount)
    }

    /**
     * @return Formatted String of crypto amount from fiat currency amount.
     */
    fun getDisplayCryptoFromFiatString(fiatText: String): String {
        val fiatAmount = fiatText.toSafeDouble(locale)
        val cryptoAmount = fiatAmount / getLastPrice()

        return getCryptoDecimalFormat().format(cryptoAmount)
    }

    //endregion

    //region Formatted displayable strings - Coin specific
    /**
     * @return BTC decimal formatted amount with appended BTC unit
     */
    fun getDisplayBtcFormatWithUnit(amount: BigInteger): String {
        val amountFormatted = btcFormat.format(Math.max(amount.toDouble(), 0.0) / 1e8)
        return "$amountFormatted ${btcUnit}"
    }

    /**
     * @return BCH decimal formatted amount with appended BTC unit
     */
    fun getDisplayBchFormatWithUnit(amount: BigInteger): String {
        val amountFormatted = btcFormat.format(Math.max(amount.toDouble(), 0.0) / 1e8)
        return "$amountFormatted ${bchUnit}"
    }

    /**
     * @return ETH decimal formatted amount with appended BTC unit
     */
    fun getDisplayEthFormatWithUnit(amount: BigDecimal): String {
        val amountFormatted = ethFormat.format(Math.max(amount.toDouble(), 0.0) / 1e8)
        return "$amountFormatted ${ethUnit}"
    }
    //endregion

    //region Formatted displayable strings - FIAT
    /**
     * Returns the current selected FIAT unit
     *
     * @return USD, GBP etc
     * @see ExchangeRateDataManager.getCurrencyLabels
     */
    fun getFiatUnit(): String {
        return prefsUtil.getValue(PrefsUtil.KEY_SELECTED_FIAT, PrefsUtil.DEFAULT_CURRENCY)
    }

    /**
     * Returns the Fiat format as a [NumberFormat] object for a given currency code.
     *
     * @param fiat The currency code (ie USD) for the format you wish to return
     * @return A [NumberFormat] object with the correct decimal fractions for the chosen Fiat format
     * @see ExchangeRateFactory.getCurrencyLabels
     */
    fun getFiatFormat(fiat: String) = fiatFormat.apply { currency = Currency.getInstance(fiat) }

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
    //endregion

    //region Convert methods
    /**
     * Returns btc amount from satoshis.
     *
     * @return btc, mbtc or bits relative to what is set in monetaryUtil
     */
    fun getTextFromSatoshis(satoshis: Long, decimalSeparator: String): String {
        var displayAmount = getFormattedCrypto(satoshis)
        displayAmount = displayAmount.replace(".", decimalSeparator)
        return displayAmount
    }

    /**
     * Returns amount of satoshis from btc amount. This could be btc, mbtc or bits.
     *
     * @return satoshis
     */
    fun getSatoshisFromText(text: String?, decimalSeparator: String): BigInteger {
        if (text == null || text.isEmpty()) return BigInteger.ZERO

        val amountToSend = stripSeparator(text, decimalSeparator)

        var amount: Double?
        try {
            amount = java.lang.Double.parseDouble(amountToSend)
        } catch (e: NumberFormatException) {
            amount = 0.0
        }

        return BigDecimal.valueOf(amount!!)
                .multiply(BigDecimal.valueOf(100000000))
                .toBigInteger()
    }

    /**
     * Returns amount of wei from ether amount.
     *
     * @return satoshis
     */
    fun getWeiFromText(text: String?, decimalSeparator: String): BigInteger {
        if (text == null || text.isEmpty()) return BigInteger.ZERO

        val amountToSend = stripSeparator(text, decimalSeparator)
        return Convert.toWei(amountToSend, Convert.Unit.ETHER).toBigInteger()
    }

    fun stripSeparator(text: String, decimalSeparator: String): String {
        return text.trim { it <= ' ' }.replace(" ", "").replace(decimalSeparator, ".")
    }

    //endregion

    companion object {
        private const val BTC_DEC = 1e8
    }
}

fun String.toSafeDouble(locale: Locale): Double {
    try {
        var amount = this
        if (amount.isEmpty()) amount = "0"
        return NumberFormat.getInstance(locale).parse(amount).toDouble()
    } catch (e: ParseException) {
        return 0.0
    }
}

fun String.toSafeLong(locale: Locale): Long {
    try {
        var amount = this
        if (amount.isEmpty()) amount = "0"
        return Math.round(NumberFormat.getInstance(locale).parse(amount).toDouble() * 1e8)
    } catch (e: ParseException) {
        return 0L
    }
}