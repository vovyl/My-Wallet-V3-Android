package piuk.blockchain.android.ui.receive

import com.blockchain.sunriver.XlmDataManager
import com.blockchain.testutils.bitcoin
import com.blockchain.testutils.bitcoinCash
import com.blockchain.testutils.ether
import com.blockchain.testutils.lumens
import com.blockchain.testutils.usd
import com.nhaarman.mockito_kotlin.atLeastOnce
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import info.blockchain.balance.AccountReference
import info.blockchain.balance.CryptoCurrency
import info.blockchain.wallet.coin.GenericMetadataAccount
import info.blockchain.wallet.ethereum.EthereumAccount
import info.blockchain.wallet.payload.PayloadManager
import info.blockchain.wallet.payload.data.Account
import info.blockchain.wallet.payload.data.AddressBook
import info.blockchain.wallet.payload.data.LegacyAddress
import info.blockchain.wallet.payload.data.archive
import io.reactivex.Single
import org.amshove.kluent.`it returns`
import org.amshove.kluent.`should be`
import org.amshove.kluent.`should equal`
import org.bitcoinj.params.BitcoinCashMainNetParams
import org.bitcoinj.params.BitcoinMainNetParams
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import piuk.blockchain.android.ui.account.ItemAccount
import piuk.blockchain.android.util.StringUtils
import piuk.blockchain.androidcore.data.api.EnvironmentConfig
import piuk.blockchain.androidcore.data.bitcoincash.BchDataManager
import piuk.blockchain.androidcore.data.currency.CurrencyState
import piuk.blockchain.androidcore.data.ethereum.EthDataManager
import piuk.blockchain.androidcore.data.ethereum.models.CombinedEthModel
import piuk.blockchain.androidcore.data.exchangerate.FiatExchangeRates
import java.math.BigInteger
import java.util.Locale

class WalletAccountHelperTest {

    private lateinit var subject: WalletAccountHelper
    private val payloadManager: PayloadManager = mock(defaultAnswer = Mockito.RETURNS_DEEP_STUBS)
    private val stringUtils: StringUtils = mock()
    private val currencyState: CurrencyState = mock()
    private val ethDataManager: EthDataManager = mock(defaultAnswer = Mockito.RETURNS_DEEP_STUBS)
    private val bchDataManager: BchDataManager = mock(defaultAnswer = Mockito.RETURNS_DEEP_STUBS)
    private val xlmDataManager: XlmDataManager = mock(defaultAnswer = Mockito.RETURNS_DEEP_STUBS)
    private val environmentSettings: EnvironmentConfig = mock()
    private val fiatExchangeRates: FiatExchangeRates = mock()

    @Before
    fun setUp() {
        Locale.setDefault(Locale.US)

        subject = WalletAccountHelper(
            payloadManager,
            stringUtils,
            currencyState,
            ethDataManager,
            bchDataManager,
            xlmDataManager,
            environmentSettings,
            fiatExchangeRates
        )

        whenever(environmentSettings.bitcoinCashNetworkParameters)
            .thenReturn(BitcoinCashMainNetParams.get())
        whenever(environmentSettings.bitcoinNetworkParameters)
            .thenReturn(BitcoinMainNetParams.get())
    }

    @Test
    fun `getAccountItems should return one Account and one LegacyAddress`() {
        // Arrange
        val label = "LABEL"
        val xPub = "X_PUB"
        val address = "ADDRESS"
        val account = Account().apply {
            this.label = label
            this.xpub = xPub
        }
        val legacyAddress = LegacyAddress().apply {
            this.label = null
            this.address = address
        }
        whenever(payloadManager.payload.hdWallets[0].accounts).thenReturn(listOf(account))
        whenever(payloadManager.payload.legacyAddressList).thenReturn(mutableListOf(legacyAddress))
        givenCryptoCurrency(CryptoCurrency.BTC)
        whenever(payloadManager.getAddressBalance(xPub)).thenReturn(1.2.bitcoin().amount)
        whenever(payloadManager.getAddressBalance(address)).thenReturn(2.3.bitcoin().amount)
        // Act
        val result = subject.getAccountItemsSingleTest(CryptoCurrency.BTC)
        // Assert
        verify(payloadManager, atLeastOnce()).payload
        result.size `should be` 2
        result[0].accountObject `should be` account
        result[0].displayBalance `should equal` "1.2 BTC"
        result[1].accountObject `should be` legacyAddress
        result[1].displayBalance `should equal` "2.3 BTC"
    }

    @Test
    fun `getAccountItems when currency is BCH should return one Account and one LegacyAddress`() {
        // Arrange
        val label = "LABEL"
        val xPub = "X_PUB"
        // Must be valid or conversion to BECH32 will fail
        val address = "17MgvXUa6tPsh3KMRWAPYBuDwbtCBF6Py5"
        val account = GenericMetadataAccount().apply {
            this.label = label
            this.xpub = xPub
        }
        val legacyAddress = LegacyAddress().apply {
            this.label = null
            this.address = address
        }
        whenever(bchDataManager.getActiveAccounts()).thenReturn(listOf(account))
        whenever(bchDataManager.getAddressBalance(address)).thenReturn(5.1.bitcoinCash().amount)
        whenever(payloadManager.payload.legacyAddressList).thenReturn(mutableListOf(legacyAddress))
        givenCryptoCurrency(CryptoCurrency.BCH)
        whenever(bchDataManager.getAddressBalance(xPub)).thenReturn(20.1.bitcoinCash().amount)
        // Act
        val result = subject.getAccountItemsSingleTest(CryptoCurrency.BCH)
        // Assert
        verify(payloadManager, atLeastOnce()).payload
        verify(bchDataManager).getActiveAccounts()
        verify(bchDataManager, atLeastOnce()).getAddressBalance(address)
        result.size `should be` 2
        result[0].accountObject `should be` account
        result[0].displayBalance `should equal` "20.1 BCH"
        result[1].accountObject `should be` legacyAddress
        result[1].displayBalance `should equal` "5.1 BCH"
    }

    @Test
    fun `getHdAccounts should return single Account`() {
        // Arrange
        val label = "LABEL"
        val xPub = "X_PUB"
        val archivedAccount = Account().apply { isArchived = true }
        val account = Account().apply {
            this.label = label
            this.xpub = xPub
        }
        whenever(payloadManager.payload.hdWallets[0].accounts)
            .thenReturn(mutableListOf(archivedAccount, account))
        givenCryptoCurrency(CryptoCurrency.BTC)
        whenever(payloadManager.getAddressBalance(xPub)).thenReturn(BigInteger.TEN)
        // Act
        val result = subject.getAccountItemsSingleTest(CryptoCurrency.BTC)
        // Assert
        verify(payloadManager, atLeastOnce()).payload
        result.size `should equal` 1
        result[0].accountObject `should be` account
        result[0].displayBalance `should equal` "0.0000001 BTC"
    }

    @Test
    fun `getHdAccounts when currency is BCH should return single Account`() {
        // Arrange
        val label = "LABEL"
        val xPub = "X_PUB"
        val archivedAccount = GenericMetadataAccount().apply { isArchived = true }
        val account = GenericMetadataAccount().apply {
            this.label = label
            this.xpub = xPub
        }
        whenever(bchDataManager.getActiveAccounts())
            .thenReturn(mutableListOf(archivedAccount, account))
        givenCryptoCurrency(CryptoCurrency.BCH)
        whenever(bchDataManager.getAddressBalance(xPub)).thenReturn(BigInteger.TEN)
        // Act
        val result = subject.getAccountItemsSingleTest(CryptoCurrency.BCH)
        // Assert
        verify(bchDataManager).getActiveAccounts()
        result.size `should equal` 1
        result[0].accountObject `should be` account
        result[0].displayBalance `should equal` "0.0000001 BCH"
    }

    @Test
    fun `getAccountItems when currency is ETH should return one account`() {
        // Arrange
        val ethAccount: EthereumAccount = mock()
        val combinedEthModel: CombinedEthModel = mock()
        givenCryptoCurrency(CryptoCurrency.ETHER)
        whenever(ethDataManager.getEthWallet()?.account).thenReturn(ethAccount)
        whenever(ethAccount.address).thenReturn("address")
        whenever(ethDataManager.getEthResponseModel()).thenReturn(combinedEthModel)
        whenever(combinedEthModel.getTotalBalance()).thenReturn(99.1.ether().amount)
        // Act
        val result = subject.getAccountItemsSingleTest(CryptoCurrency.ETHER)
        // Assert
        verify(ethDataManager, atLeastOnce()).getEthWallet()
        result.size `should be` 1
        result[0].accountObject `should equal` ethAccount
        result[0].displayBalance `should equal` "99.1 ETH"
    }

    @Test
    fun `getAccountItems when currency is XLM should return one account`() {
        // Arrange
        givenCryptoCurrency(CryptoCurrency.XLM)
        whenever(xlmDataManager.defaultAccount()) `it returns`
            Single.just(
                AccountReference.Xlm(
                    "My Xlm account",
                    "address"
                )
            )
        whenever(xlmDataManager.getBalance()) `it returns` Single.just(123.lumens())
        // Act
        val result = subject.accountItems(currencyState.cryptoCurrency)
            .test().values().single()
        // Assert
        result.size `should be` 1
        result[0].label `should equal` "My Xlm account"
        result[0].displayBalance `should equal` "123.0 XLM"
    }

    @Test
    fun `getLegacyAddresses should return single LegacyAddress`() {
        // Arrange
        val address = "ADDRESS"
        val archivedAddress = LegacyAddress().apply { archive() }
        val legacyAddress = LegacyAddress().apply {
            this.label = null
            this.address = address
        }
        whenever(payloadManager.payload.legacyAddressList)
            .thenReturn(mutableListOf(archivedAddress, legacyAddress))
        givenCryptoCurrency(CryptoCurrency.BTC)
        whenever(payloadManager.getAddressBalance(address)).thenReturn(BigInteger.TEN)
        // Act
        val result = subject.getLegacyAddresses()
        // Assert
        verify(payloadManager, atLeastOnce()).payload
        result.size `should equal` 1
        result[0].accountObject `should be` legacyAddress
        result[0].displayBalance `should equal` "0.0000001 BTC"
    }

    @Test
    fun `getAddressBookEntries should return single item`() {
        // Arrange
        val addressBook = AddressBook()
        whenever(payloadManager.payload.addressBook).thenReturn(listOf(addressBook))
        // Act
        val result = subject.getAddressBookEntries()
        // Assert
        result.size `should equal` 1
    }

    @Test
    fun `getAddressBookEntries should return empty list`() {
        // Arrange
        whenever(payloadManager.payload.addressBook)
            .thenReturn(null)
        // Act
        val result = subject.getAddressBookEntries()
        // Assert
        result.size `should equal` 0
    }

    @Test
    fun `getDefaultAccount should return ETH account`() {
        // Arrange
        val ethAccount: EthereumAccount = mock()
        val combinedEthModel: CombinedEthModel = mock()
        givenCryptoCurrency(CryptoCurrency.ETHER)
        whenever(ethDataManager.getEthWallet()?.account).thenReturn(ethAccount)
        whenever(ethAccount.address).thenReturn("address")
        whenever(ethDataManager.getEthResponseModel()).thenReturn(combinedEthModel)
        whenever(combinedEthModel.getTotalBalance()).thenReturn(123.7.ether().amount)
        // Act
        val result = subject.getDefaultAccount()
        // Assert
        verify(ethDataManager, atLeastOnce()).getEthWallet()
        result.accountObject `should equal` ethAccount
        result.displayBalance `should equal` "123.7 ETH"
    }

    @Test
    fun `getDefaultAccount should return BTC account`() {
        // Arrange
        val btcAccount: Account = mock()
        whenever(btcAccount.xpub).thenReturn("xpub")
        givenCryptoCurrency(CryptoCurrency.BTC)
        whenever(payloadManager.payload.hdWallets[0].defaultAccountIdx).thenReturn(0)
        whenever(payloadManager.payload.hdWallets[0].accounts[0]).thenReturn(btcAccount)
        whenever(payloadManager.getAddressBalance("xpub")).thenReturn(BigInteger.TEN)
        // Act
        val result = subject.getDefaultAccount()
        // Assert
        verify(payloadManager, atLeastOnce()).payload
        result.accountObject `should be` btcAccount
        result.displayBalance `should equal` "0.0000001 BTC"
    }

    @Test
    fun `getDefaultAccount fiat balance display`() {
        // Arrange
        givenCryptoCurrency(CryptoCurrency.BTC)
        whenever(currencyState.displayMode) `it returns` CurrencyState.DisplayMode.Fiat
        whenever(fiatExchangeRates.getFiat(100.bitcoin())) `it returns` 300.99.usd()

        val btcAccount: Account = mock()
        whenever(btcAccount.xpub).thenReturn("xpub")
        whenever(payloadManager.payload.hdWallets[0].defaultAccountIdx).thenReturn(0)
        whenever(payloadManager.payload.hdWallets[0].accounts[0]).thenReturn(btcAccount)
        whenever(payloadManager.getAddressBalance("xpub")).thenReturn(100.bitcoin().amount)
        // Act
        val result = subject.getDefaultAccount()
        // Assert
        verify(payloadManager, atLeastOnce()).payload
        result.accountObject `should be` btcAccount
        result.displayBalance `should equal` "$300.99"
    }

    @Test
    fun `getDefaultAccount should return BCH account`() {
        // Arrange
        val bchAccount: GenericMetadataAccount = mock()
        whenever(bchAccount.xpub).thenReturn("")
        givenCryptoCurrency(CryptoCurrency.BCH)
        whenever(bchDataManager.getDefaultGenericMetadataAccount()).thenReturn(bchAccount)
        whenever(bchDataManager.getAddressBalance("")).thenReturn(BigInteger.TEN)
        // Act
        val result = subject.getDefaultAccount()
        // Assert
        verify(bchDataManager).getDefaultGenericMetadataAccount()
        result.accountObject `should equal` bchAccount
        result.displayBalance `should equal` "0.0000001 BCH"
    }

    private fun givenCryptoCurrency(cryptoCurrency: CryptoCurrency) {
        whenever(currencyState.displayMode) `it returns` CurrencyState.DisplayMode.Crypto
        whenever(currencyState.cryptoCurrency) `it returns` cryptoCurrency
    }
}

private fun WalletAccountHelper.getAccountItemsSingleTest(cryptoCurrency: CryptoCurrency): List<ItemAccount> =
    accountItems(cryptoCurrency).test().values().single()
