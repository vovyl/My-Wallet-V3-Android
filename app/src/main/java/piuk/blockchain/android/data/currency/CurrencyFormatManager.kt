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
class CurrencyFormatManager(private val currencyState: CurrencyState,
                            private val exchangeRateDataManager: ExchangeRateDataManager,
                            private val prefsUtil: PrefsUtil,
                            private val currencyFormatUtil: CurrencyFormatUtil,
                            private val locale: Locale) {

    enum class CoinDenomination() {
        SATOSHI,
        BTC,
        WEI,
        ETH
    }

    //region Selected Coin methods based on CurrencyState.currencyState
    /**
     * Returns the maximum decimals allowed for current crypto currency state. Useful to apply max decimal length
     * on text fields.
     *
     * @return decimal length.
     */
    fun getSelectedCoinMaxFractionDigits() =
            when (currencyState.cryptoCurrency) {
                CryptoCurrencies.BTC -> currencyFormatUtil.getBtcMaxFractionDigits()
                CryptoCurrencies.ETHER -> currencyFormatUtil.getEthMaxFractionDigits()
                CryptoCurrencies.BCH -> currencyFormatUtil.getBchMaxFractionDigits()
                else -> throw IllegalArgumentException(currencyState.cryptoCurrency.toString() + " not supported.")
            }

    /**
     * Crypto unit based on current crypto currency state.
     *
     * @return BTC, BCH or ETH.
     */
    fun getSelectedCoinUnit() =
            when (currencyState.cryptoCurrency) {
                CryptoCurrencies.BTC -> currencyFormatUtil.getBtcUnit()
                CryptoCurrencies.ETHER -> currencyFormatUtil.getEthUnit()
                CryptoCurrencies.BCH -> currencyFormatUtil.getBchUnit()
                else -> throw IllegalArgumentException(currencyState.cryptoCurrency.toString() + " not supported.")
            }

    /**
     * Accepts a [Long] value in Satoshis/Wei and returns the display amount as a [String] based on the
     * chosen [unit] type.
     *
     * eg. 10_000 Satoshi -> "0.0001" when unit == UNIT_BTC
     * eg. 1_000_000_000_000_000_000 Wei -> "1.0" when unit == UNIT_ETH
     *
     * @param value The amount to be formatted in Satoshis
     * @return An amount formatted as a [String]
     */
    fun getSelectedCoinValue(satoshisOrWei: Long) =
            when (currencyState.cryptoCurrency) {
                CryptoCurrencies.BTC, CryptoCurrencies.BCH -> getSelectedCoinValue(getBtcFromSatoshis(satoshisOrWei))
                CryptoCurrencies.ETHER -> getSelectedCoinValue(getEthFromWei(satoshisOrWei))
                else -> throw IllegalArgumentException(currencyState.cryptoCurrency.toString() + " not supported.")
            }

    fun getSelectedCoinValue(btcOrEth: BigDecimal) =
            when (currencyState.cryptoCurrency) {
                CryptoCurrencies.BTC, CryptoCurrencies.BCH -> currencyFormatUtil.formatBtc(btcOrEth)
                CryptoCurrencies.ETHER -> currencyFormatUtil.formatEth(btcOrEth)
                else -> throw IllegalArgumentException(currencyState.cryptoCurrency.toString() + " not supported.")
            }

    fun getSelectedCoinValueWithUnit(satoshisOrWei: Long): String {
        return "${getSelectedCoinValue(satoshisOrWei)} ${getSelectedCoinUnit()}"
    }

    fun getSelectedCoinValueWithUnit(btcOrEther: BigDecimal): String {
        return "${getSelectedCoinValue(btcOrEther)} ${getSelectedCoinUnit()}"
    }

//    private fun getSelectedCoinDecimalFormat() =
//            when (currencyState.cryptoCurrency) {
//                CryptoCurrencies.BTC -> btcFormat
//                CryptoCurrencies.ETHER -> ethFormat
//                CryptoCurrencies.BCH -> btcFormat
//                else -> throw IllegalArgumentException(currencyState.cryptoCurrency.toString() + " not supported.")
//            }

    /**
     * @return Last known exchange rate based on current crypto currency state.
     */
    fun getSelectedCoinLastPrice(): Double {
        when (currencyState.cryptoCurrency) {
            CryptoCurrencies.BTC -> return exchangeRateDataManager.getLastBtcPrice(getFiatUnit())
            CryptoCurrencies.ETHER -> return exchangeRateDataManager.getLastEthPrice(getFiatUnit())
            CryptoCurrencies.BCH -> return exchangeRateDataManager.getLastBchPrice(getFiatUnit())
            else -> throw IllegalArgumentException(currencyState.cryptoCurrency.toString() + " not supported.")
        }
    }

    /**
     * @return Formatted String of crypto amount from fiat currency amount.
     */
    fun getSelectedCoinValueFromFiatString(fiatText: String): String {

        val fiatAmount = fiatText.toSafeDouble(locale).toBigDecimal()

        return when (currencyState.cryptoCurrency) {
            CryptoCurrencies.BTC -> currencyFormatUtil.formatBtc(exchangeRateDataManager.getBtcFromFiat(fiatAmount, getFiatUnit()))
            CryptoCurrencies.ETHER -> currencyFormatUtil.formatEth(exchangeRateDataManager.getEthFromFiat(fiatAmount, getFiatUnit()))
            CryptoCurrencies.BCH -> currencyFormatUtil.formatBch(exchangeRateDataManager.getBchFromFiat(fiatAmount, getFiatUnit()))
            else -> throw IllegalArgumentException(currencyState.cryptoCurrency.toString() + " not supported.")
        }
    }
    //endregion

    //region Fiat methods
    /**
     * Returns the currency's country code
     *
     * @return The currency abbreviation (USD, GBP etc)
     * @see ExchangeRateDataManager.getCurrencyLabels
     */
    fun getFiatUnit(): String {
        return prefsUtil.getValue(PrefsUtil.KEY_SELECTED_FIAT, PrefsUtil.DEFAULT_CURRENCY)
    }

    /**
     * Returns the symbol for the current chosen currency, based on the passed currency code and the chosen
     * device [Locale].
     *
     * @return The correct currency symbol (eg. "$")
     */
    fun getFiatSymbol(): String =
            Currency.getInstance(getFiatUnit()).getSymbol(locale)

    /**
     * Returns the symbol for the chosen currency, based on the passed currency code and the chosen
     * device [Locale].
     *
     * @param currencyCode The 3-letter currency code, eg. "GBP"
     * @param locale The current device [Locale]
     * @return The correct currency symbol (eg. "$")
     */
    fun getFiatSymbol(currencyCode: String, locale: Locale): String =
            Currency.getInstance(currencyCode).getSymbol(locale)

    //TODO This should be private but there are a few places that still use this
    fun getFiatFormat(currencyCode: String) = currencyFormatUtil.getFiatFormat(currencyCode)

    private fun getFiatValueFromSelectedCoin(coinValue: BigDecimal, convertDenomination: CoinDenomination): BigDecimal {

        val fiatUnit = getFiatUnit()

        val sanitizedDenomination = when (convertDenomination) {
            CoinDenomination.BTC -> coinValue
            CoinDenomination.SATOSHI -> coinValue.divide(BTC_DEC.toBigDecimal())
            CoinDenomination.ETH -> coinValue
            CoinDenomination.WEI -> coinValue.divide(ETH_DEC.toBigDecimal())
        }

        val fiatBalance = when (currencyState.cryptoCurrency) {
            CryptoCurrencies.BTC -> exchangeRateDataManager.getFiatFromBtc(sanitizedDenomination, fiatUnit)
            CryptoCurrencies.ETHER -> exchangeRateDataManager.getFiatFromEth(sanitizedDenomination, fiatUnit)
            CryptoCurrencies.BCH -> exchangeRateDataManager.getFiatFromBch(sanitizedDenomination, fiatUnit)
            else -> throw IllegalArgumentException(currencyState.cryptoCurrency.toString() + " not supported.")
        }

        return fiatBalance
    }

    fun getFiatValueFromSelectedCoinValue(coinValue: BigDecimal, convertDenomination: CoinDenomination): String {
        val fiatUnit = getFiatUnit()
        val fiatBalance = getFiatValueFromSelectedCoin(coinValue, convertDenomination)
        return currencyFormatUtil.formatFiat(fiatBalance, fiatUnit)
    }

    fun getFiatValueFromSelectedCoinValueWithUnit(coinValue: BigDecimal, convertDenomination: CoinDenomination): String {
        val fiatUnit = getFiatUnit()
        val fiatBalance = getFiatValueFromSelectedCoin(coinValue, convertDenomination)
        return currencyFormatUtil.formatFiatWithUnit(fiatBalance, fiatUnit)
    }

    /**
     * Returns a formatted fiat string based on the input text and last known exchange rate.
     * If the input text can't be cast to a double this will return 0.0
     *
     * @return Formatted String of fiat amount from coin amount.
     */
    fun getFiatValueFromCoinValueInputText(coinInputText: String, convertDenomination: CoinDenomination): String {
        val cryptoAmount = coinInputText.toSafeDouble(locale).toBigDecimal()
        return getFiatValueFromSelectedCoinValue(cryptoAmount, convertDenomination)
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
            this.currencySymbol = getFiatSymbol(currencyCode, locale)
        }
        return numberFormat.format(amount)
    }
    //endregion

    //region Coin specific methods
    /**
     * @return BTC decimal formatted amount with appended BTC unit
     */
    fun getBtcValueWithUnit(satoshis: Long): String {
        return getBtcValueWithUnit((satoshis.toDouble() / BTC_DEC).toBigDecimal())
    }

    /**
     * @return BCH decimal formatted amount with appended BCH unit
     */
    fun getBchValueWithUnit(satoshis: Long): String {
        return getBchValueWithUnit((satoshis.toDouble() / BTC_DEC).toBigDecimal())
    }

    /**
     * @return ETH decimal formatted amount with appended ETH unit
     */
    fun getEthValueWithUnit(amount: Long): String {
        return getEthValueWithUnit((amount.toDouble() / ETH_DEC).toBigDecimal())
    }

    fun getBtcValueWithUnit(btc: BigDecimal): String {
        return currencyFormatUtil.formatBtcWithUnit(btc)
    }

    /**
     * @return BCH decimal formatted amount with appended BCH unit
     */
    fun getBchValueWithUnit(bch: BigDecimal): String {
        return currencyFormatUtil.formatBchWithUnit(bch)
    }

    /**
     * @return ETH decimal formatted amount with appended ETH unit
     */
    fun getEthValueWithUnit(eth: BigDecimal): String {
        return currencyFormatUtil.formatEthWithUnit(eth)
    }

    //endregion

    //region Convert methods
    /**
     * Returns btc amount from satoshis.
     *
     * @return btc, mbtc or bits relative to what is set in monetaryUtil
     */
    fun getTextFromSatoshis(satoshis: Long, decimalSeparator: String): String {
        var displayAmount = getSelectedCoinValue(satoshis)
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

    //region Conversion methods
    fun getBtcFromSatoshis(satoshis: Long) =
            BigDecimal.valueOf(satoshis / BTC_DEC)

    fun getSatoshisFromBtc(btc: BigDecimal) = btc.multiply(BTC_DEC.toBigDecimal()).toLong()

    fun getWeiFromEth(eth: BigDecimal) =
            Convert.toWei(eth, Convert.Unit.ETHER)

    fun getEthFromWei(wei: Long) =
            Convert.fromWei(wei.toBigDecimal(), Convert.Unit.ETHER)
    //endregion

    companion object {
        private const val BTC_DEC = 1e8
        private const val ETH_DEC = 1e18
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