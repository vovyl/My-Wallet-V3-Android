package piuk.blockchain.android.ui.transactions

import android.content.Intent
import com.blockchain.android.testutils.rxInit
import com.blockchain.sunriver.XlmDataManager
import com.blockchain.testutils.stroops
import com.blockchain.testutils.usd
import com.nhaarman.mockito_kotlin.atLeastOnce
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import info.blockchain.balance.AccountReference
import info.blockchain.balance.CryptoCurrency
import info.blockchain.wallet.multiaddress.TransactionSummary
import info.blockchain.wallet.payload.data.Wallet
import io.reactivex.Completable
import io.reactivex.Single
import junit.framework.Assert.assertEquals
import org.amshove.kluent.any
import org.amshove.kluent.mock
import org.apache.commons.lang3.tuple.Pair
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Answers
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import piuk.blockchain.android.R
import piuk.blockchain.android.data.datamanagers.TransactionListDataManager
import piuk.blockchain.android.data.datamanagers.models.XlmDisplayable
import piuk.blockchain.android.ui.balance.BalanceFragment.Companion.KEY_TRANSACTION_HASH
import piuk.blockchain.android.ui.balance.BalanceFragment.Companion.KEY_TRANSACTION_LIST_POSITION
import piuk.blockchain.android.util.StringUtils
import piuk.blockchain.androidcore.data.api.EnvironmentConfig
import piuk.blockchain.androidcore.data.bitcoincash.BchDataManager
import piuk.blockchain.androidcore.data.ethereum.EthDataManager
import piuk.blockchain.androidcore.data.exchangerate.ExchangeRateDataManager
import piuk.blockchain.androidcore.data.payload.PayloadDataManager
import piuk.blockchain.androidcore.data.transactions.models.BchDisplayable
import piuk.blockchain.androidcore.data.transactions.models.BtcDisplayable
import piuk.blockchain.androidcore.data.transactions.models.Displayable
import piuk.blockchain.androidcore.data.transactions.models.EthDisplayable
import piuk.blockchain.androidcore.utils.PrefsUtil
import piuk.blockchain.androidcoreui.ui.customviews.ToastCustom
import java.math.BigInteger
import java.util.Arrays
import java.util.HashMap
import java.util.Locale

class TransactionDetailPresenterTest {

    private lateinit var subject: TransactionDetailPresenter
    private val transactionHelper: TransactionHelper = mock()
    private val prefsUtil: PrefsUtil = mock()
    private val payloadDataManager: PayloadDataManager = mock()
    private val stringUtils: StringUtils = mock()
    private val transactionListDataManager: TransactionListDataManager = mock()
    private val view: TransactionDetailView = mock()
    private val exchangeRateFactory: ExchangeRateDataManager = mock(defaultAnswer = Answers.RETURNS_DEEP_STUBS)
    private val ethDataManager: EthDataManager = mock(defaultAnswer = Answers.RETURNS_DEEP_STUBS)
    private val bchDataManager: BchDataManager = mock()
    private val environmentSettings: EnvironmentConfig = mock()
    private val xlmDataManager: XlmDataManager = mock()

    @get:Rule
    val rxSchedulers = rxInit {
        mainTrampoline()
        ioTrampoline()
    }

    @Before
    fun setUp() {
        whenever(prefsUtil.getValue(PrefsUtil.KEY_SELECTED_FIAT, PrefsUtil.DEFAULT_CURRENCY))
            .thenReturn(PrefsUtil.DEFAULT_CURRENCY)

        Locale.setDefault(Locale("EN", "US"))
        subject = TransactionDetailPresenter(
            transactionHelper,
            prefsUtil,
            payloadDataManager,
            stringUtils,
            transactionListDataManager,
            exchangeRateFactory,
            ethDataManager,
            bchDataManager,
            environmentSettings,
            xlmDataManager
        )
        subject.initView(view)
    }

    @Test
    fun onViewReadyNoIntent() {
        // Arrange
        whenever(view.getPageIntent()).thenReturn(null)
        // Act
        subject.onViewReady()
        // Assert
        verify(view).getPageIntent()
        verify(view).pageFinish()
        verifyNoMoreInteractions(view)
    }

    @Test
    fun onViewReadyNoKey() {
        // Arrange
        val mockIntent: Intent = mock()
        whenever(mockIntent.hasExtra(KEY_TRANSACTION_LIST_POSITION)).thenReturn(false)
        whenever(view.getPageIntent()).thenReturn(mockIntent)
        // Act
        subject.onViewReady()
        // Assert
        verify(view).getPageIntent()
        verify(view).pageFinish()
        verifyNoMoreInteractions(view)
    }

    @Test
    fun onViewReadyKeyOutOfBounds() {
        // Arrange
        val mockIntent: Intent = mock()
        whenever(mockIntent.hasExtra(KEY_TRANSACTION_LIST_POSITION)).thenReturn(true)
        whenever(mockIntent.getIntExtra(KEY_TRANSACTION_LIST_POSITION, -1)).thenReturn(-1)
        whenever(view.getPageIntent()).thenReturn(mockIntent)
        // Act
        subject.onViewReady()
        // Assert
        verify(view).getPageIntent()
        verify(view).pageFinish()
        verifyNoMoreInteractions(view)
    }

    @Test
    fun onViewReadyNullIntent() {
        // Arrange
        whenever(view.getPageIntent()).thenReturn(null)
        // Act
        subject.onViewReady()
        // Assert
        verify(view).getPageIntent()
        verify(view).pageFinish()
        verifyNoMoreInteractions(view)
    }

    @Test
    fun onViewReadyIntentPositionInvalid() {
        // Arrange
        val mockIntent: Intent = mock()
        whenever(mockIntent.getIntExtra(KEY_TRANSACTION_LIST_POSITION, -1)).thenReturn(-1)
        whenever(view.getPageIntent()).thenReturn(mockIntent)
        // Act
        subject.onViewReady()
        // Assert
        verify(view).getPageIntent()
        verify(view).pageFinish()
        verifyNoMoreInteractions(view)
    }

    @Test
    fun onViewReadyIntentHashNotFound() {
        // Arrange
        val mockIntent: Intent = mock()
        val txHash = "TX_HASH"
        whenever(mockIntent.getStringExtra(KEY_TRANSACTION_HASH)).thenReturn(txHash)
        whenever(view.getPageIntent()).thenReturn(mockIntent)
        whenever(transactionListDataManager.getTxFromHash(txHash))
            .thenReturn(Single.error(Throwable()))
        // Act
        subject.onViewReady()
        // Assert
        verify(view).getPageIntent()
        verify(view).pageFinish()
        verifyNoMoreInteractions(view)
    }

    @Test
    fun onViewReadyTransactionFoundInList() {
        // Arrange
        val displayableToFind: BtcDisplayable = mock()
        whenever(displayableToFind.cryptoCurrency).thenReturn(CryptoCurrency.BTC)
        whenever(displayableToFind.direction).thenReturn(TransactionSummary.Direction.TRANSFERRED)
        whenever(displayableToFind.hash).thenReturn("txMoved_hash")
        whenever(displayableToFind.total).thenReturn(BigInteger.valueOf(1_000L))
        whenever(displayableToFind.fee).thenReturn(BigInteger.valueOf(1L))

        val displayable2: BtcDisplayable = mock()
        whenever(displayable2.hash).thenReturn("")

        val displayable3: BtcDisplayable = mock()
        whenever(displayable3.hash).thenReturn("")

        val mockIntent: Intent = mock()
        val mockPayload: Wallet = mock()
        whenever(mockIntent.hasExtra(KEY_TRANSACTION_LIST_POSITION)).thenReturn(true)
        whenever(mockIntent.getIntExtra(KEY_TRANSACTION_LIST_POSITION, -1)).thenReturn(0)
        whenever(mockPayload.txNotes).thenReturn(HashMap())
        whenever(view.getPageIntent()).thenReturn(mockIntent)
        whenever(payloadDataManager.wallet).thenReturn(mockPayload)
        whenever(transactionListDataManager.getTransactionList())
            .thenReturn(Arrays.asList<Displayable>(displayableToFind, displayable2, displayable3))
        whenever(stringUtils.getString(R.string.transaction_detail_pending))
            .thenReturn("Pending (%1\$s/%2\$s Confirmations)")
        val inputs = HashMap<String, BigInteger>()
        val outputs = HashMap<String, BigInteger>()
        inputs["addr1"] = BigInteger.valueOf(1000L)
        outputs["addr2"] = BigInteger.valueOf(2000L)
        val pair = Pair.of(inputs, outputs)
        whenever(transactionHelper.filterNonChangeAddresses(any())).thenReturn(pair)
        whenever(payloadDataManager.addressToLabel("addr1")).thenReturn("account1")
        whenever(payloadDataManager.addressToLabel("addr2")).thenReturn("account2")
        whenever(exchangeRateFactory.getHistoricPrice(any(), any(), any()))
            .thenReturn(Single.just(1000.usd()))
        whenever(stringUtils.getString(R.string.transaction_detail_value_at_time_transferred))
            .thenReturn("Value when moved: ")
        // Act
        subject.onViewReady()
        // Assert
        verify(view).getPageIntent()
        verify(view).setStatus(
            CryptoCurrency.BTC,
            "Pending (0/3 Confirmations)",
            "txMoved_hash"
        )
        verify(view).setTransactionType(TransactionSummary.Direction.TRANSFERRED)
        verify(view).setTransactionColour(R.color.product_gray_transferred_50)
        verify(view).setDescription(null)
        verify(view).setDate(any())
        verify(view).setToAddresses(any())
        verify(view).setFromAddress(any())
        verify(view).setFee(any())
        verify(view).setTransactionValue(any())
        verify(view).setTransactionValueFiat(any())
        verify(view).onDataLoaded()
        verify(view).setIsDoubleSpend(any())
        verifyNoMoreInteractions(view)
    }

    @Test
    fun onViewReadyTransactionFoundViaHash() {
        // Arrange
        val displayable: BtcDisplayable = mock()
        whenever(displayable.cryptoCurrency).thenReturn(CryptoCurrency.BTC)
        whenever(displayable.direction).thenReturn(TransactionSummary.Direction.TRANSFERRED)
        whenever(displayable.hash).thenReturn("txMoved_hash")
        whenever(displayable.total).thenReturn(BigInteger.valueOf(1_000L))
        whenever(displayable.fee).thenReturn(BigInteger.valueOf(1L))
        val mockIntent: Intent = mock()
        val mockPayload: Wallet = mock()
        whenever(mockIntent.hasExtra(KEY_TRANSACTION_HASH)).thenReturn(true)
        whenever(mockIntent.getStringExtra(KEY_TRANSACTION_HASH)).thenReturn("txMoved_hash")
        whenever(view.getPageIntent()).thenReturn(mockIntent)
        whenever(mockPayload.txNotes).thenReturn(HashMap())
        whenever(payloadDataManager.wallet).thenReturn(mockPayload)
        whenever(view.getPageIntent()).thenReturn(mockIntent)
        whenever(transactionListDataManager.getTxFromHash("txMoved_hash"))
            .thenReturn(Single.just(displayable))
        whenever(stringUtils.getString(R.string.transaction_detail_pending))
            .thenReturn("Pending (%1\$s/%2\$s Confirmations)")
        val inputs = HashMap<String, BigInteger>()
        val outputs = HashMap<String, BigInteger>()
        inputs["addr1"] = BigInteger.valueOf(1000L)
        outputs["addr2"] = BigInteger.valueOf(2000L)
        val pair = Pair.of(inputs, outputs)
        whenever(transactionHelper.filterNonChangeAddresses(any())).thenReturn(pair)
        whenever(payloadDataManager.addressToLabel("addr1")).thenReturn("account1")
        whenever(payloadDataManager.addressToLabel("addr2")).thenReturn("account2")
        whenever(exchangeRateFactory.getHistoricPrice(any(), any(), any()))
            .thenReturn(Single.just(1000.usd()))
        whenever(stringUtils.getString(R.string.transaction_detail_value_at_time_transferred))
            .thenReturn("Value when moved: ")
        // Act
        subject.onViewReady()
        // Assert
        verify(view).getPageIntent()
        verify(view).setStatus(
            CryptoCurrency.BTC,
            "Pending (0/3 Confirmations)",
            "txMoved_hash"
        )
        verify(view).setTransactionType(TransactionSummary.Direction.TRANSFERRED)
        verify(view).setTransactionColour(R.color.product_gray_transferred_50)
        verify(view).setDescription(null)
        verify(view).setDate(any())
        verify(view).setToAddresses(any())
        verify(view).setFromAddress(any())
        verify(view).setFee(any())
        verify(view).setTransactionValue(any())
        verify(view).setTransactionValueFiat(any())
        verify(view).onDataLoaded()
        verify(view).setIsDoubleSpend(any())
        verifyNoMoreInteractions(view)
    }

    @Test
    fun onViewReadyTransactionFoundViaHashEthereum() {
        // Arrange
        val displayable: BtcDisplayable = mock()
        whenever(displayable.cryptoCurrency).thenReturn(CryptoCurrency.ETHER)
        whenever(displayable.direction).thenReturn(TransactionSummary.Direction.SENT)
        whenever(displayable.hash).thenReturn("hash")
        whenever(displayable.total).thenReturn(BigInteger.valueOf(1_000L))
        whenever(displayable.fee).thenReturn(BigInteger.valueOf(1L))
        val maps = HashMap<String, BigInteger>()
        maps[""] = BigInteger.TEN
        whenever(displayable.inputsMap).thenReturn(maps)
        whenever(displayable.outputsMap).thenReturn(maps)
        val mockIntent: Intent = mock()
        whenever(mockIntent.hasExtra(KEY_TRANSACTION_HASH)).thenReturn(true)
        whenever(mockIntent.getStringExtra(KEY_TRANSACTION_HASH)).thenReturn("hash")
        whenever(view.getPageIntent()).thenReturn(mockIntent)
        whenever(transactionListDataManager.getTxFromHash("hash"))
            .thenReturn(Single.just(displayable))
        whenever(stringUtils.getString(R.string.transaction_detail_pending))
            .thenReturn("Pending (%1\$s/%2\$s Confirmations)")
        whenever(stringUtils.getString(R.string.eth_default_account_label))
            .thenReturn("My Ethereum Wallet")
        val inputs = HashMap<String, BigInteger>()
        val outputs = HashMap<String, BigInteger>()
        inputs["addr1"] = BigInteger.valueOf(1000L)
        outputs["addr2"] = BigInteger.valueOf(2000L)
        val pair = Pair.of(inputs, outputs)
        whenever(transactionHelper.filterNonChangeAddresses(any())).thenReturn(pair)
        whenever(exchangeRateFactory.getHistoricPrice(any(), any(), any()))
            .thenReturn(Single.just(1000.usd()))
        whenever(stringUtils.getString(R.string.transaction_detail_value_at_time_sent))
            .thenReturn("Value when sent: ")
        whenever(ethDataManager.getEthResponseModel()!!.getAddressResponse()!!.account).thenReturn("")
        whenever(ethDataManager.getTransactionNotes("hash")).thenReturn("note")
        // Act
        subject.onViewReady()
        // Assert
        verify(view).getPageIntent()
        verify(view).setStatus(CryptoCurrency.ETHER, "Pending (0/12 Confirmations)", "hash")
        verify(view).setTransactionType(TransactionSummary.Direction.SENT)
        verify(view).setTransactionColour(R.color.product_red_sent_50)
        verify(view).setDescription(any())
        verify(view).setDate(any())
        verify(view).setToAddresses(any())
        verify(view).setFromAddress(any())
        verify(view).setFee(any())
        verify(view).setTransactionValue(any())
        verify(view).setTransactionValueFiat(any())
        verify(view).onDataLoaded()
        verify(view).setIsDoubleSpend(any())
        verifyNoMoreInteractions(view)
    }

    @Test
    fun `onViewReady transaction found via hash xlm`() {
        // Arrange
        val displayable: XlmDisplayable = mock()
        whenever(displayable.cryptoCurrency).thenReturn(CryptoCurrency.XLM)
        whenever(displayable.direction).thenReturn(TransactionSummary.Direction.SENT)
        whenever(displayable.hash).thenReturn("hash")
        whenever(displayable.total).thenReturn(BigInteger.valueOf(1_000L))
        whenever(displayable.fee).thenReturn(BigInteger.valueOf(1L))
        val maps = HashMap<String, BigInteger>()
        maps[""] = BigInteger.TEN
        whenever(displayable.inputsMap).thenReturn(maps)
        whenever(displayable.outputsMap).thenReturn(maps)
        val mockIntent: Intent = mock()
        whenever(mockIntent.hasExtra(KEY_TRANSACTION_HASH)).thenReturn(true)
        whenever(mockIntent.getStringExtra(KEY_TRANSACTION_HASH)).thenReturn("hash")
        whenever(view.getPageIntent()).thenReturn(mockIntent)
        whenever(transactionListDataManager.getTxFromHash("hash"))
            .thenReturn(Single.just(displayable))
        whenever(stringUtils.getString(R.string.transaction_detail_pending))
            .thenReturn("Pending (%1\$s/%2\$s Confirmations)")
        whenever(stringUtils.getString(R.string.xlm_default_account_label))
            .thenReturn("My Lumens Wallet")
        whenever(stringUtils.getString(R.string.transaction_detail_value_at_time_sent))
            .thenReturn("Value when sent: ")
        whenever(xlmDataManager.defaultAccount())
            .thenReturn(Single.just(AccountReference.Xlm("My Lumens Wallet", "Account ID")))
        whenever(xlmDataManager.getTransactionFee("hash")).thenReturn(Single.just(100.stroops()))
        whenever(exchangeRateFactory.getHistoricPrice(any(), any(), any()))
            .thenReturn(Single.just(1000.usd()))
        // Act
        subject.onViewReady()
        // Assert
        verify(view).getPageIntent()
        verify(view).setStatus(CryptoCurrency.XLM, "Pending (0/1 Confirmations)", "hash")
        verify(view).setTransactionType(TransactionSummary.Direction.SENT)
        verify(view).setTransactionColour(R.color.product_red_sent_50)
        verify(view).setDescription(any())
        verify(view).setDate(any())
        verify(view).setToAddresses(any())
        verify(view).setFromAddress(any())
        verify(view, atLeastOnce()).setFee(any())
        verify(view).hideDescriptionField()
        verify(view).setTransactionValue(any())
        verify(view).setTransactionValueFiat(any())
        verify(view).onDataLoaded()
        verify(view).setIsDoubleSpend(any())
        verifyNoMoreInteractions(view)
    }

    @Test
    fun getTransactionValueStringUsd() {
        // Arrange
        val displayable: BtcDisplayable = mock()
        whenever(displayable.cryptoCurrency).thenReturn(CryptoCurrency.BTC)
        whenever(displayable.direction).thenReturn(TransactionSummary.Direction.SENT)
        whenever(displayable.total).thenReturn(BigInteger.valueOf(1_000L))
        whenever(exchangeRateFactory.getHistoricPrice(any(), any(), any()))
            .thenReturn(Single.just(1000.usd()))
        whenever(stringUtils.getString(any())).thenReturn("Value when sent: ")
        // Act
        val observer = subject.getTransactionValueString("USD", displayable).test()

        // Assert
        verify(exchangeRateFactory).getHistoricPrice(any(), any(), any())
        assertEquals("Value when sent: $1,000.00", observer.values()[0])
        observer.onComplete()
        observer.assertNoErrors()
    }

    @Test
    fun getTransactionValueStringReceivedEth() {
        // Arrange
        val displayable: BtcDisplayable = mock()
        whenever(displayable.cryptoCurrency).thenReturn(CryptoCurrency.ETHER)
        whenever(displayable.direction).thenReturn(TransactionSummary.Direction.RECEIVED)
        whenever(displayable.total).thenReturn(BigInteger.valueOf(1_000L))
        whenever(exchangeRateFactory.getHistoricPrice(any(), any(), any()))
            .thenReturn(Single.just(1000.usd()))
        whenever(stringUtils.getString(any())).thenReturn("Value when received: ")
        // Act
        val observer = subject.getTransactionValueString("USD", displayable).test()
        // Assert
        verify(exchangeRateFactory).getHistoricPrice(any(), any(), any())
        assertEquals("Value when received: $1,000.00", observer.values()[0])
        observer.onComplete()
        observer.assertNoErrors()
    }

    @Test
    fun getTransactionValueStringTransferred() {
        // Arrange
        val displayable: BtcDisplayable = mock()
        whenever(displayable.cryptoCurrency).thenReturn(CryptoCurrency.BTC)
        whenever(displayable.direction).thenReturn(TransactionSummary.Direction.SENT)
        whenever(displayable.total).thenReturn(BigInteger.valueOf(1_000L))
        whenever(exchangeRateFactory.getHistoricPrice(any(), any(), any()))
            .thenReturn(Single.just(1000.usd()))
        whenever(stringUtils.getString(any())).thenReturn("Value when transferred: ")
        whenever(prefsUtil.getValue(PrefsUtil.KEY_SELECTED_FIAT, PrefsUtil.DEFAULT_CURRENCY))
            .thenReturn("USD")
        // Act
        val observer = subject.getTransactionValueString("USD", displayable).test()
        // Assert
        verify(exchangeRateFactory).getHistoricPrice(any(), any(), any())
        assertEquals("Value when transferred: $1,000.00", observer.values()[0])
        observer.onComplete()
        observer.assertNoErrors()
    }

    @Test
    fun updateTransactionNoteBtcSuccess() {
        // Arrange
        val displayable: BtcDisplayable = mock()
        whenever(displayable.hash).thenReturn("hash")
        whenever(displayable.cryptoCurrency).thenReturn(CryptoCurrency.BTC)
        subject.displayable = displayable
        whenever(payloadDataManager.updateTransactionNotes(any(), any()))
            .thenReturn(Completable.complete())
        // Act
        subject.updateTransactionNote("note")
        // Assert
        verify(payloadDataManager).updateTransactionNotes("hash", "note")

        verify(view).showToast(R.string.remote_save_ok, ToastCustom.TYPE_OK)
        verify(view).setDescription("note")
    }

    @Test
    fun updateTransactionNoteEthSuccess() {
        // Arrange
        val displayable: BtcDisplayable = mock()
        whenever(displayable.hash).thenReturn("hash")
        whenever(displayable.cryptoCurrency).thenReturn(CryptoCurrency.ETHER)
        subject.displayable = displayable
        whenever(ethDataManager.updateTransactionNotes(any(), any()))
            .thenReturn(Completable.complete())
        // Act
        subject.updateTransactionNote("note")
        // Assert
        verify(ethDataManager).updateTransactionNotes("hash", "note")

        verify(view).showToast(R.string.remote_save_ok, ToastCustom.TYPE_OK)
        verify(view).setDescription("note")
    }

    @Test
    fun updateTransactionNoteFailure() {
        // Arrange
        val displayable: BtcDisplayable = mock()
        whenever(displayable.hash).thenReturn("hash")
        whenever(displayable.cryptoCurrency).thenReturn(CryptoCurrency.BTC)
        subject.displayable = displayable
        whenever(payloadDataManager.updateTransactionNotes(any(), any()))
            .thenReturn(Completable.error(Throwable()))
        // Act
        subject.updateTransactionNote("note")
        // Assert
        verify(payloadDataManager).updateTransactionNotes("hash", "note")

        verify(view).showToast(R.string.unexpected_error, ToastCustom.TYPE_ERROR)
    }

    @Test(expected = IllegalArgumentException::class)
    fun updateTransactionNoteBchSuccess() {
        // Arrange
        val displayable: BtcDisplayable = mock()
        whenever(displayable.hash).thenReturn("hash")
        whenever(displayable.cryptoCurrency).thenReturn(CryptoCurrency.BCH)
        subject.displayable = displayable
        whenever(ethDataManager.updateTransactionNotes(any(), any()))
            .thenReturn(Completable.complete())
        // Act
        subject.updateTransactionNote("note")
        // Assert
    }

    @Test
    fun getTransactionNoteBtc() {
        // Arrange
        val displayable: BtcDisplayable = mock()
        whenever(displayable.hash).thenReturn("hash")
        whenever(displayable.cryptoCurrency).thenReturn(CryptoCurrency.BTC)
        subject.displayable = displayable
        whenever(payloadDataManager.getTransactionNotes("hash")).thenReturn("note")
        // Act
        val value = subject.transactionNote
        // Assert
        assertEquals("note", value)
        verify(payloadDataManager).getTransactionNotes("hash")
    }

    @Test
    fun getTransactionNoteEth() {
        // Arrange
        val displayable: BtcDisplayable = mock()
        whenever(displayable.hash).thenReturn("hash")
        whenever(displayable.cryptoCurrency).thenReturn(CryptoCurrency.ETHER)
        subject.displayable = displayable
        whenever(ethDataManager.getTransactionNotes("hash")).thenReturn("note")
        // Act
        val value = subject.transactionNote
        // Assert
        assertEquals("note", value)
        verify(ethDataManager).getTransactionNotes("hash")
    }

    @Test
    fun getTransactionNoteBch() {
        // Arrange
        val displayable: BchDisplayable = mock()
        whenever(displayable.hash).thenReturn("hash")
        whenever(displayable.cryptoCurrency).thenReturn(CryptoCurrency.BCH)
        subject.displayable = displayable
        // Act
        val value = subject.transactionNote
        // Assert
        assertEquals("", value)
    }

    @Test
    fun getTransactionHash() {
        // Arrange
        val displayable: BtcDisplayable = mock()
        whenever(displayable.hash).thenReturn("hash")
        subject.displayable = displayable
        // Act
        val value = subject.transactionHash
        // Assert
        assertEquals("hash", value)
    }

    @Test
    fun setTransactionStatusNoConfirmations() {
        // Arrange
        whenever(stringUtils.getString(R.string.transaction_detail_pending))
            .thenReturn("Pending (%1\$s/%2\$s Confirmations)")
        // Act
        subject.setConfirmationStatus(CryptoCurrency.ETHER, "hash", 0)
        // Assert
        verify(view).setStatus(CryptoCurrency.ETHER, "Pending (0/12 Confirmations)", "hash")
        verifyNoMoreInteractions(view)
    }

    @Test
    fun setTransactionStatusConfirmed() {
        // Arrange
        whenever(stringUtils.getString(R.string.transaction_detail_confirmed)).thenReturn("Confirmed")
        // Act
        subject.setConfirmationStatus(CryptoCurrency.BTC, "hash", 3)
        // Assert
        verify(view).setStatus(CryptoCurrency.BTC, "Confirmed", "hash")
        verifyNoMoreInteractions(view)
    }

    @Test
    fun setTransactionColorMove() {
        // Arrange
        val displayable: BtcDisplayable = mock()
        whenever(displayable.confirmations).thenReturn(0)
        whenever(displayable.cryptoCurrency).thenReturn(CryptoCurrency.BTC)
        whenever(displayable.direction).thenReturn(TransactionSummary.Direction.TRANSFERRED)
        // Act
        subject.setTransactionColor(displayable)
        // Assert
        verify(view).setTransactionColour(R.color.product_gray_transferred_50)
        verifyNoMoreInteractions(view)
    }

    @Test
    fun setTransactionColorMoveConfirmed() {
        // Arrange
        val displayable: BtcDisplayable = mock()
        whenever(displayable.confirmations).thenReturn(3)
        whenever(displayable.cryptoCurrency).thenReturn(CryptoCurrency.BTC)
        whenever(displayable.direction).thenReturn(TransactionSummary.Direction.TRANSFERRED)
        // Act
        subject.setTransactionColor(displayable)
        // Assert
        verify(view).setTransactionColour(R.color.product_gray_transferred)
        verifyNoMoreInteractions(view)
    }

    @Test
    fun setTransactionColorSent() {
        // Arrange
        val displayable: BtcDisplayable = mock()
        whenever(displayable.confirmations).thenReturn(2)
        whenever(displayable.cryptoCurrency).thenReturn(CryptoCurrency.BTC)
        whenever(displayable.direction).thenReturn(TransactionSummary.Direction.SENT)
        // Act
        subject.setTransactionColor(displayable)
        // Assert
        verify(view).setTransactionColour(R.color.product_red_sent_50)
        verifyNoMoreInteractions(view)
    }

    @Test
    fun setTransactionColorSentConfirmed() {
        // Arrange
        val displayable: BtcDisplayable = mock()
        whenever(displayable.confirmations).thenReturn(3)
        whenever(displayable.cryptoCurrency).thenReturn(CryptoCurrency.BTC)
        whenever(displayable.direction).thenReturn(TransactionSummary.Direction.SENT)
        // Act
        subject.setTransactionColor(displayable)
        // Assert
        verify(view).setTransactionColour(R.color.product_red_sent)
        verifyNoMoreInteractions(view)
    }

    @Test
    fun setTransactionColorReceived() {
        // Arrange
        val displayable: EthDisplayable = mock()
        whenever(displayable.confirmations).thenReturn(7)
        whenever(displayable.cryptoCurrency).thenReturn(CryptoCurrency.ETHER)
        whenever(displayable.direction).thenReturn(TransactionSummary.Direction.RECEIVED)
        // Act
        subject.setTransactionColor(displayable)
        // Assert
        verify(view).setTransactionColour(R.color.product_green_received_50)
        verifyNoMoreInteractions(view)
    }

    @Test
    fun setTransactionColorReceivedConfirmed() {
        // Arrange
        val displayable: BtcDisplayable = mock()
        whenever(displayable.confirmations).thenReturn(3)
        whenever(displayable.direction).thenReturn(TransactionSummary.Direction.RECEIVED)
        whenever(displayable.cryptoCurrency).thenReturn(CryptoCurrency.BTC)
        // Act
        subject.setTransactionColor(displayable)
        // Assert
        verify(view).setTransactionColour(R.color.product_green_received)
        verifyNoMoreInteractions(view)
    }
}