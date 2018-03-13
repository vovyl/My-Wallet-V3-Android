package piuk.blockchain.android.data.currency

import org.web3j.utils.Convert
import piuk.blockchain.android.data.exchangerate.ExchangeRateDataManager
import piuk.blockchain.android.util.MonetaryUtil
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

    fun getCryptoMaxDecimalLength() =
            when (currencyState.cryptoCurrency) {
                CryptoCurrencies.BTC -> maxBtcDecimalLength
                CryptoCurrencies.ETHER -> maxEthDecimalLength
                CryptoCurrencies.BCH -> maxBtcDecimalLength
                else -> throw IllegalArgumentException(currencyState.cryptoCurrency.toString() + " not supported.")
            }

    fun getCryptoUnit() =
        when (currencyState.cryptoCurrency) {
            CryptoCurrencies.BTC -> btcUnit
            CryptoCurrencies.ETHER -> ethUnit
            CryptoCurrencies.BCH -> bchUnit
            else -> throw IllegalArgumentException(currencyState.cryptoCurrency.toString() + " not supported.")
        }

    private fun getCryptoDecimalFormat() =
            when (currencyState.cryptoCurrency) {
                CryptoCurrencies.BTC -> btcFormat
                CryptoCurrencies.ETHER -> ethFormat
                CryptoCurrencies.BCH -> btcFormat
                else -> throw IllegalArgumentException(currencyState.cryptoCurrency.toString() + " not supported.")
            }

    fun getFiatUnit(): String {
        return prefsUtil.getValue(PrefsUtil.KEY_SELECTED_FIAT, PrefsUtil.DEFAULT_CURRENCY)
    }

    //todo no need to expose
    fun getLastPrice(): Double {
        when (currencyState.cryptoCurrency) {
            CryptoCurrencies.BTC -> return exchangeRateDataManager.getLastBtcPrice(getFiatUnit())
            CryptoCurrencies.ETHER -> return exchangeRateDataManager.getLastEthPrice(getFiatUnit())
            CryptoCurrencies.BCH -> return exchangeRateDataManager.getLastBchPrice(getFiatUnit())
            else -> throw IllegalArgumentException(currencyState.cryptoCurrency.toString() + " not supported.")
        }
    }

    /**
     * Mostly use this method since we alter between crypto and fiat in various parts of the app which
     * means you don't have to handle currency state.
     * @return Fiat or Crypto decimal formatted amount with unit based on current currencyState
     */
    fun getDisplayFormatWithUnit(amount: BigInteger): String {

        return if (currencyState.isDisplayingCryptoCurrency) {
            getDisplayCryptoFormatWithUnit(amount)
        } else {
            getDisplayFiatFormatWithUnit(amount)
        }
    }

    /**
     * @return Crypto decimal formatted amount with unit based on current currencyState
     */
    fun getDisplayCryptoFormatWithUnit(amount: BigInteger): String {
        val amountFormatted = getCryptoDecimalFormat().format(Math.max(amount.toDouble(), 0.0) / 1e8)
        return "$amountFormatted ${getCryptoUnit()}"
    }

    /**
     * @return Crypto decimal formatted amount based on current currencyState
     */
    fun getDisplayCryptoFormat(amount: BigInteger): String {
        val amountFormatted = getCryptoDecimalFormat().format(Math.max(amount.toDouble(), 0.0) / 1e8)
        return "$amountFormatted"
    }

    /**
     * @return Crypto decimal formatted amount based on current currencyState
     */
    fun getDisplayCryptoFormat(amountText: String): String {

        var amount = amountText
        if (amount.isEmpty()) amount = "0"

        val amountFormatted = getCryptoDecimalFormat().format(Math.max(amount.toDouble(), 0.0) / 1e8)
        return "$amountFormatted"
    }

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

    /**
     * @return ETH decimal formatted amount
     */
    fun getDisplayEthFormat(amount: BigDecimal): String {
        val amountFormatted = ethFormat.format(Math.max(amount.toDouble(), 0.0) / 1e8)
        return "$amountFormatted"
    }


    /**
     * @return Fiat decimal formatted amount with unit based on current currencyState
     */
    fun getDisplayFiatFormatWithUnit(amount: BigInteger): String {
        val fiatBalance = getLastPrice() * (Math.max(amount.toDouble(), 0.0) / 1e8)
        val fiatBalanceFormatted = fiatFormat.format(getFiatUnit()).format(fiatBalance)

        return "$fiatBalanceFormatted ${getFiatUnit()}"
    }

    /**
     * @return Fiat decimal formatted amount with unit based on current currencyState
     */
    fun getDisplayFiatFormatWithUnit(amountText: String): String {
        return "${getDisplayFiatFormat(amountText)} ${getFiatUnit()}"
    }

    /**
     * @return Fiat decimal formatted amount based on current currencyState
     */
    fun getDisplayFiatFormat(amountText: String): String {

        var amount = amountText
        if (amount.isEmpty()) amount = "0"

        val fiatBalance = getLastPrice() * (Math.max(amount.toDouble(), 0.0) / 1e8)
        val fiatBalanceFormatted = fiatFormat.format(getFiatUnit()).format(fiatBalance)

        return "$fiatBalanceFormatted"
    }

    fun getFormattedFiatStringFromCryptoString(bitcoin: String): String {
        var amount = bitcoin
        if (amount.isEmpty()) amount = "0"
        val btcAmount = getDoubleAmount(amount)
        val fiatAmount = getLastPrice() * btcAmount

        return fiatFormat.format(fiatAmount)
    }

    fun getFormattedCryptoStringFromFiatString(fiat: String): String {
        var amount = fiat
        if (amount.isEmpty()) amount = "0"
        val fiatAmount = getDoubleAmount(amount)
        val cryptoAmount = fiatAmount / getLastPrice()

        return getCryptoDecimalFormat().format(cryptoAmount)
    }

    fun getFormattedCryptoStringFromFiat(fiatAmount: Double): String {

        val cryptoAmount = fiatAmount / getLastPrice()

        return if (currencyState.cryptoCurrency === CryptoCurrencies.ETHER) {
            ethFormat.format(BigDecimal.valueOf(cryptoAmount))
        } else {
            btcFormat.format(cryptoAmount)
        }
    }

    /**
     * Parse String value to region formatted double
     *
     * @param amount A string to be parsed
     * @return The amount as a double, formatted for the current region
     */
    fun getDoubleAmount(amount: String): Double {
        try {
            return NumberFormat.getInstance(locale).parse(amount).toDouble()
        } catch (e: ParseException) {
            return 0.0
        }

    }

    /**
     * Parse String value to region formatted long
     *
     * @param amount A string to be parsed
     * @return The amount as a long, formatted for the current region
     */
    fun getLongAmount(amount: String): Long {
        try {
            return Math.round(NumberFormat.getInstance(locale).parse(amount).toDouble() * 1e8)
        } catch (e: ParseException) {
            return 0L
        }

    }

    fun getFormattedEthString(eth: BigDecimal) = ethFormat.format(eth)

    fun getFormattedFiatStringFromCrypto(cryptoAmount: Double): String {
        val fiatAmount = getLastPrice() * cryptoAmount
        return fiatFormat.format(fiatAmount)
    }

    /**
     * Return false if value is higher than the sum of all Bitcoin in future existence
     *
     * @param amount A [BigInteger] amount of Bitcoin in BTC
     * @return True if amount higher than 21 Million
     */
    fun getIfAmountInvalid(amount: BigInteger): Boolean {
        return amount.compareTo(BigInteger.valueOf(2100000000000000L)) == 1
    }

    /**
     * Returns btc amount from satoshis.
     *
     * @return btc, mbtc or bits relative to what is set in monetaryUtil
     */
    fun getTextFromSatoshis(satoshis: Long, decimalSeparator: String): String {
        var displayAmount = getDisplayAmount(satoshis)
        displayAmount = displayAmount.replace(".", decimalSeparator)
        return displayAmount
    }

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
    fun getDisplayAmount(value: Long): String = btcFormat.format(value / BTC_DEC)

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

    companion object {
        private const val BTC_DEC = 1e8
    }
}