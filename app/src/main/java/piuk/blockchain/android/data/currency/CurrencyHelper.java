package piuk.blockchain.android.data.currency;

import org.web3j.utils.Convert;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

import piuk.blockchain.android.data.currency.CryptoCurrencies;
import piuk.blockchain.android.data.currency.CurrencyState;
import piuk.blockchain.android.data.currency.ExchangeRateDataManager;
import piuk.blockchain.android.util.MonetaryUtil;
import piuk.blockchain.android.util.PrefsUtil;

public class CurrencyHelper {

    private MonetaryUtil monetaryUtil;
    private Locale locale;
    private final PrefsUtil prefsUtil;
    private final ExchangeRateDataManager exchangeRateFactory;
    private CurrencyState currencyState;

    public CurrencyHelper(MonetaryUtil monetaryUtil,
                          Locale locale,
                          PrefsUtil prefsUtil,
                          ExchangeRateDataManager exchangeRateFactory,
                          CurrencyState currencyState) {
        this.monetaryUtil = monetaryUtil;
        this.locale = locale;
        this.prefsUtil = prefsUtil;
        this.exchangeRateFactory = exchangeRateFactory;
        this.currencyState = currencyState;
    }

    /**
     * Get saved BTC unit
     *
     * @return The saved BTC unit
     */
    public String getBtcUnit() {
        return CryptoCurrencies.BTC.name();
    }

    /**
     * Get saved ETH unit
     *
     * @return The saved ETH unit
     */
    public String getEthUnit() {
        return CryptoCurrencies.ETHER.name();
    }

    /**
     * Get saved BCH unit - BCH, mBCH or bits
     *
     * @return The saved BCH unit
     */
    public String getBchUnit() {
        return CryptoCurrencies.BCH.name();
    }

    public String getCryptoUnit() {
        switch (currencyState.getCryptoCurrency()){
            case BTC: return getBtcUnit();
            case ETHER: return getEthUnit();
            case BCH: return getBchUnit();
            default: throw new IllegalArgumentException(currencyState.getCryptoCurrency()+" not supported.");
        }
    }

    /**
     * Get save Fiat currency unit
     *
     * @return The saved Fiat unit
     */
    public String getFiatUnit() {
        return prefsUtil.getValue(PrefsUtil.KEY_SELECTED_FIAT, PrefsUtil.DEFAULT_CURRENCY);
    }

    /**
     * Get last price for saved currency
     *
     * @return A double exchange rate
     */
    public double getLastPrice() {
        switch (currencyState.getCryptoCurrency()){
            case BTC: return exchangeRateFactory.getLastBtcPrice(getFiatUnit());
            case ETHER: return exchangeRateFactory.getLastEthPrice(getFiatUnit());
            case BCH: return exchangeRateFactory.getLastBchPrice(getFiatUnit());
            default: throw new IllegalArgumentException(currencyState.getCryptoCurrency()+" not supported.");
        }
    }

    /**
     * Get correctly formatted BTC currency String, ie with region specific separator
     *
     * @param amount The amount of Bitcoin in either BTC, mBits or bits
     * @return A region formatted BTC string for the saved unit
     */
    public String getFormattedBtcString(double amount) {
        return monetaryUtil.getBtcFormat().format(amount);
    }

    /**
     * Get correctly formatted Fiat currency String, ie with region specific separator
     *
     * @param amount The amount of currency as a double
     * @return A region formatted string
     */
    public String getFormattedFiatString(double amount) {
        return monetaryUtil.getFiatFormat(getFiatUnit()).format(amount);
    }

    /**
     * Get correctly formatted ETH currency String, ie with region specific separator
     *
     * @param amount The amount of Ether in ETH
     * @return A region formatted ETH string for the saved unit
     */
    public String getFormattedEthString(BigDecimal amount) {
        return monetaryUtil.getEthFormat().format(amount);
    }

    public String getFormattedCryptoStringFromFiat(double fiatAmount) {

        double cryptoAmount = fiatAmount / getLastPrice();

        if (currencyState.getCryptoCurrency() == CryptoCurrencies.ETHER) {
            return getFormattedEthString(BigDecimal.valueOf(cryptoAmount));
        } else {
            return getFormattedBtcString(cryptoAmount);
        }
    }
    public String getFormattedFiatStringFromCrypto(double cryptoAmount) {
        double fiatAmount = getLastPrice() * cryptoAmount;
        return getFormattedFiatString(fiatAmount);
    }

    /**
     * Get the maximum number of decimal points for the saved BTC unit
     *
     * @return The max number of allowed decimal points
     */
    public int getMaxBtcDecimalLength() {
        int maxLength = 8;
        return maxLength;
    }

    /**
     * Get the maximum number of decimal points for the current Crypto unit
     *
     * @return The max number of allowed decimal points
     */
    public int getMaxCryptoDecimalLength() {
        if (currencyState.getCryptoCurrency() == CryptoCurrencies.BTC) {
            return getMaxBtcDecimalLength();
        } else {
            return 18;
        }
    }

    /**
     * Parse String value to region formatted long
     *
     * @param amount A string to be parsed
     * @return The amount as a long, formatted for the current region
     */
    public long getLongAmount(String amount) {
        try {
            return Math.round(NumberFormat.getInstance(locale).parse(amount).doubleValue() * 1e8);
        } catch (ParseException e) {
            return 0L;
        }
    }

    /**
     * Parse String value to region formatted double
     *
     * @param amount A string to be parsed
     * @return The amount as a double, formatted for the current region
     */
    public double getDoubleAmount(String amount) {
        try {
            return NumberFormat.getInstance(locale).parse(amount).doubleValue();
        } catch (ParseException e) {
            return 0D;
        }
    }

    /**
     * Return false if value is higher than the sum of all Bitcoin in future existence
     *
     * @param amount A {@link BigInteger} amount of Bitcoin in BTC
     * @return True if amount higher than 21 Million
     */
    public boolean getIfAmountInvalid(BigInteger amount) {
        return amount.compareTo(BigInteger.valueOf(2100000000000000L)) == 1;
    }

    /**
     * Returns btc amount from satoshis.
     *
     * @return btc, mbtc or bits relative to what is set in monetaryUtil
     */
    public String getTextFromSatoshis(long satoshis, String decimalSeparator) {
        String displayAmount = monetaryUtil.getDisplayAmount(satoshis);
        displayAmount = displayAmount.replace(".", decimalSeparator);
        return displayAmount;
    }

    /**
     * Returns amount of satoshis from btc amount. This could be btc, mbtc or bits.
     *
     * @return satoshis
     */
    public BigInteger getSatoshisFromText(String text, String decimalSeparator) {
        if (text == null || text.isEmpty()) return BigInteger.ZERO;

        String amountToSend = stripSeparator(text, decimalSeparator);

        Double amount;
        try {
            amount = java.lang.Double.parseDouble(amountToSend);
        } catch (NumberFormatException e) {
            amount = 0.0;
        }

        return BigDecimal.valueOf(amount)
                .multiply(BigDecimal.valueOf(100000000))
                .toBigInteger();
    }

    /**
     * Returns amount of wei from ether amount.
     *
     * @return satoshis
     */
    public BigInteger getWeiFromText(String text, String decimalSeparator) {
        if (text == null || text.isEmpty()) return BigInteger.ZERO;

        String amountToSend = stripSeparator(text, decimalSeparator);
        return Convert.toWei(amountToSend, Convert.Unit.ETHER).toBigInteger();
    }

    public String stripSeparator(String text, String decimalSeparator) {
        return text.trim().replace(" ","").replace(decimalSeparator, ".");
    }
}
