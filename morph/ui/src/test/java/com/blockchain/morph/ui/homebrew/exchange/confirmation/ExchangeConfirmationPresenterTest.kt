package com.blockchain.morph.ui.homebrew.exchange.confirmation

import com.blockchain.android.testutils.rxInit
import com.blockchain.datamanagers.TransactionExecutorWithoutFees
import com.blockchain.morph.exchange.service.TradeExecutionService
import com.blockchain.payload.PayloadDecrypt
import com.blockchain.testutils.bitcoin
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import info.blockchain.balance.AccountReference
import info.blockchain.balance.CryptoCurrency
import info.blockchain.balance.CryptoValue
import io.reactivex.Observable
import io.reactivex.Single
import org.amshove.kluent.mock
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import piuk.blockchain.androidcoreui.ui.customviews.ToastCustom
import java.util.Locale

class ExchangeConfirmationPresenterTest {

    private lateinit var subject: ExchangeConfirmationPresenter
    private val transactionExecutor: TransactionExecutorWithoutFees = mock()
    private val tradeExecutionService: TradeExecutionService = mock()
    private val payloadDecrypt: PayloadDecrypt = mock()
    private val view: ExchangeConfirmationView = mock()

    @get:Rule
    val rxSchedulers = rxInit {
        mainTrampoline()
        ioTrampoline()
    }

    @Before
    fun setUp() {
        subject = ExchangeConfirmationPresenter(
            transactionExecutor,
            tradeExecutionService,
            payloadDecrypt
        )
        subject.initView(view)

        whenever(view.locale).thenReturn(Locale.US)
    }

    @Test
    fun `on view ready double encrypted`() {
        // Arrange
        whenever(payloadDecrypt.isDoubleEncrypted).thenReturn(true)
        whenever(view.exchangeViewState).thenReturn(Observable.empty())
        // Act
        subject.onViewReady()
        // Assert
        verify(view).showSecondPasswordDialog()
    }

    @Test
    fun `update fee success`() {
        // Arrange
        val accountReference = AccountReference.BitcoinLike(CryptoCurrency.BTC, "Label", "xPub")
        whenever(payloadDecrypt.isDoubleEncrypted).thenReturn(false)
        val fee = CryptoValue.bitcoinFromMajor(0.00005.toBigDecimal())
        whenever(
            transactionExecutor.getFeeForTransaction(
                1.0.bitcoin(),
                accountReference
            )
        ).thenReturn(Single.just(fee))
        // Act
        subject.updateFee(1.0.bitcoin(), accountReference)
        // Assert
        verify(view).updateFee(fee)
    }

    @Test
    fun `update fee failure`() {
        // Arrange
        val accountReference = AccountReference.BitcoinLike(CryptoCurrency.BTC, "Label", "xPub")
        whenever(payloadDecrypt.isDoubleEncrypted).thenReturn(false)
        whenever(
            transactionExecutor.getFeeForTransaction(
                1.0.bitcoin(),
                accountReference
            )
        ).thenReturn(Single.error { Throwable() })
        // Act
        subject.updateFee(1.0.bitcoin(), accountReference)
        // Assert
        verify(view).showToast(any(), eq(ToastCustom.TYPE_ERROR))
    }
}
