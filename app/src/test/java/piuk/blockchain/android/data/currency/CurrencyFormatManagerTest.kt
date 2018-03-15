package piuk.blockchain.android.data.currency

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.whenever
import org.amshove.kluent.mock
import org.junit.Before
import piuk.blockchain.android.data.exchangerate.ExchangeRateDataManager
import piuk.blockchain.android.util.PrefsUtil
import java.math.BigDecimal
import java.math.BigInteger
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals

class CurrencyFormatManagerTest {

    private lateinit var subject: CurrencyFormatManager
    private val currencyState: CurrencyState = mock()
    private val exchangeRateDataManager: ExchangeRateDataManager = mock()
    private val currencyFormatUtil: CurrencyFormatUtil = mock()
    private val prefsUtil: PrefsUtil = mock()
    private val locale = Locale.getDefault()

    @Before
    fun setUp() {
        subject = CurrencyFormatManager(
                currencyState,
                exchangeRateDataManager,
                prefsUtil,
                currencyFormatUtil,
                locale
        )
    }

    //region Current selected crypto currency state methods
    @Test
    fun `getCryptoMaxDecimalLength BTC`() {
        // Arrange
        whenever(currencyState.cryptoCurrency).thenReturn(CryptoCurrencies.BTC)

        // Act
        val result = subject.getSelectedCoinMaxFractionDigits()

        // Assert
        assertEquals(8, result)
    }

    @Test
    fun `getCryptoMaxDecimalLength BCH`() {
        // Arrange
        whenever(currencyState.cryptoCurrency).thenReturn(CryptoCurrencies.BCH)

        // Act
        val result = subject.getSelectedCoinMaxFractionDigits()

        // Assert
        assertEquals(8, result)
    }

    @Test
    fun `getCryptoMaxDecimalLength ETH`() {
        // Arrange
        whenever(currencyState.cryptoCurrency).thenReturn(CryptoCurrencies.ETHER)

        // Act
        val result = subject.getSelectedCoinMaxFractionDigits()

        // Assert
        assertEquals(18, result)
    }

    @Test
    fun `getCryptoUnit BTC`() {
        // Arrange
        whenever(currencyState.cryptoCurrency).thenReturn(CryptoCurrencies.BTC)

        // Act
        val result = subject.getSelectedCoinUnit()

        // Assert
        assertEquals("BTC", result)
    }

    @Test
    fun `getCryptoUnit BCH`() {
        // Arrange
        whenever(currencyState.cryptoCurrency).thenReturn(CryptoCurrencies.BCH)

        // Act
        val result = subject.getSelectedCoinUnit()

        // Assert
        assertEquals("BCH", result)
    }

    @Test
    fun `getCryptoUnit ETH`() {
        // Arrange
        whenever(currencyState.cryptoCurrency).thenReturn(CryptoCurrencies.ETHER)

        // Act
        val result = subject.getSelectedCoinUnit()

        // Assert
        assertEquals("ETH", result)
    }

    @Test
    fun `getDisplayCrypto BTC`() {
        // Arrange
        whenever(currencyState.cryptoCurrency).thenReturn(CryptoCurrencies.BTC)

        // Act
        // Assert
        assertEquals("0.00000001", subject.getSelectedCoinValue(1L))
        assertEquals("0.0001", subject.getSelectedCoinValue(10_000L))
        assertEquals("100,000.0", subject.getSelectedCoinValue((100_000 * 1e8).toLong()))
        assertEquals("1.0", subject.getSelectedCoinValue(1e8.toLong()))
    }

    @Test
    fun `getDisplayCrypto BCH`() {
        // Arrange
        whenever(currencyState.cryptoCurrency).thenReturn(CryptoCurrencies.BCH)

        // Act
        // Assert
        assertEquals("0.00000001", subject.getSelectedCoinValue(1L))
        assertEquals("0.0001", subject.getSelectedCoinValue(10_000L))
        assertEquals("100,000.0", subject.getSelectedCoinValue((100_000 * 1e8).toLong()))
        assertEquals("1.0", subject.getSelectedCoinValue(1e8.toLong()))
    }

    @Test
    fun `getDisplayCrypto ETH`() {
        // Arrange
        whenever(currencyState.cryptoCurrency).thenReturn(CryptoCurrencies.ETHER)

        // Act
        // Assert
        assertEquals("1.00", subject.getSelectedCoinValue(1_000_000_000_000_000_000L))
        assertEquals("0.000000000000000001", subject.getSelectedCoinValue(1L))
    }

    @Test
    fun `getLastPrice BTC`() {
        // Arrange
        whenever(currencyState.cryptoCurrency).thenReturn(CryptoCurrencies.BTC)
        whenever(exchangeRateDataManager.getLastBtcPrice(any())).thenReturn(1.0)
        whenever(prefsUtil.getValue(PrefsUtil.KEY_SELECTED_FIAT, PrefsUtil.DEFAULT_CURRENCY))
                .thenReturn(PrefsUtil.DEFAULT_CURRENCY)

        // Act
        val result = subject.getSelectedCoinLastPrice()

        // Assert
        assertEquals(1.0, result)
    }

    @Test
    fun `getLastPrice BCH`() {
        // Arrange
        whenever(currencyState.cryptoCurrency).thenReturn(CryptoCurrencies.BCH)
        whenever(exchangeRateDataManager.getLastBchPrice(any())).thenReturn(1.1)
        whenever(prefsUtil.getValue(PrefsUtil.KEY_SELECTED_FIAT, PrefsUtil.DEFAULT_CURRENCY))
                .thenReturn(PrefsUtil.DEFAULT_CURRENCY)

        // Act
        val result = subject.getSelectedCoinLastPrice()

        // Assert
        assertEquals(1.1, result)
    }

    @Test
    fun `getLastPrice ETH`() {
        // Arrange
        whenever(currencyState.cryptoCurrency).thenReturn(CryptoCurrencies.ETHER)
        whenever(exchangeRateDataManager.getLastEthPrice(any())).thenReturn(1.2)
        whenever(prefsUtil.getValue(PrefsUtil.KEY_SELECTED_FIAT, PrefsUtil.DEFAULT_CURRENCY))
                .thenReturn(PrefsUtil.DEFAULT_CURRENCY)

        // Act
        val result = subject.getSelectedCoinLastPrice()

        // Assert
        assertEquals(1.2, result)
    }

    //endregion

    @Test
    fun `getDisplayAmountWithUnit BTC`() {
        // Arrange
        whenever(currencyState.isDisplayingCryptoCurrency).thenReturn(true)
        whenever(currencyState.cryptoCurrency).thenReturn(CryptoCurrencies.BTC)

        // Act
        val result = subject.getSelectedCoinValueWithUnit(BigDecimal.valueOf(10_000L))

        // Assert
        assertEquals("0.0001 BTC", result)
    }
//
//    @Test
//    fun `getDisplayAmountWithUnit BCH`() {
//        // Arrange
//        whenever(currencyState.isDisplayingCryptoCurrency).thenReturn(true)
//        whenever(currencyState.cryptoCurrency).thenReturn(CryptoCurrencies.BCH)
//
//        // Act
//        val result = subject.getSelectedCoinValueWithUnit(BigInteger.valueOf(10_000L))
//
//        // Assert
//        assertEquals("0.0001 BCH", result)
//    }
//
//    @Test
//    fun `getDisplayAmountWithUnit ETH`() {
//        // Arrange
//        whenever(currencyState.isDisplayingCryptoCurrency).thenReturn(true)
//        whenever(currencyState.cryptoCurrency).thenReturn(CryptoCurrencies.ETHER)
//
//        // Act
//        val result = subject.getSelectedCoinValueWithUnit(BigInteger.valueOf(10_000L))
//
//        // Assert
//        assertEquals("0.00000000000001 ETH", result)
//    }
}