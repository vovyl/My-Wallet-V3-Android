package com.blockchain.morph.ui.homebrew.exchange.confirmation

import com.blockchain.android.testutils.rxInit
import com.blockchain.datamanagers.TransactionExecutorWithoutFees
import com.blockchain.morph.exchange.mvi.ExchangeViewState
import com.blockchain.morph.exchange.mvi.Fix
import com.blockchain.morph.exchange.mvi.Quote
import com.blockchain.morph.exchange.service.TradeExecutionService
import com.blockchain.morph.exchange.service.TradeTransaction
import com.blockchain.morph.to
import com.blockchain.payload.PayloadDecrypt
import com.blockchain.testutils.bitcoin
import com.blockchain.testutils.ether
import com.blockchain.testutils.lumens
import com.blockchain.testutils.usd
import com.blockchain.transactions.Memo
import com.blockchain.transactions.SendDetails
import com.blockchain.transactions.SendException
import com.blockchain.transactions.SendFundsResult
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
import com.nhaarman.mockito_kotlin.whenever
import info.blockchain.balance.AccountReference
import info.blockchain.balance.CryptoCurrency
import info.blockchain.balance.CryptoValue
import info.blockchain.balance.withMajorValue
import info.blockchain.wallet.exceptions.TransactionHashApiException
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import org.amshove.kluent.`it returns`
import org.amshove.kluent.mock
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import piuk.blockchain.androidcoreui.ui.customviews.ToastCustom
import java.math.BigDecimal
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

    @Test
    fun `a successful trade`() {
        val fromAccount = AccountReference.BitcoinLike(CryptoCurrency.BTC, "", "")
        val toAccount = AccountReference.Ethereum("", "")
        val (exchangeViewSubject, quote) = givenTradeExpected(fromAccount, toAccount)
        whenever(
            transactionExecutor.executeTransaction(
                1.bitcoin(),
                "SERVER_DEPOSIT_ADDRESS",
                fromAccount,
                Memo("MEMO_BODY", "text")
            )
        ).thenReturn(Single.just("TX_HASH"))
        subject.onViewReady()
        exchangeViewSubject.onNext(mock {
            on { latestQuote } `it returns` quote
            on { this.fromAccount } `it returns` fromAccount
            on { this.toAccount } `it returns` toAccount
        })
        verify(transactionExecutor).getReceiveAddress(fromAccount)
        verify(transactionExecutor).getReceiveAddress(toAccount)
        verify(transactionExecutor).executeTransaction(any(), any(), any(), any())
        verifyNoMoreInteractions(transactionExecutor)
        verify(tradeExecutionService).executeTrade(any(), any(), any())
        verifyNoMoreInteractions(tradeExecutionService)
        verify(view).continueToExchangeLocked(any())
    }

    @Test
    fun `a push tx error trade`() {
        val fromAccount = AccountReference.BitcoinLike(CryptoCurrency.BTC, "", "")
        val toAccount = AccountReference.Ethereum("", "")
        val (exchangeViewSubject, quote, tradeTransaction) = givenTradeExpected(fromAccount, toAccount)
        whenever(
            transactionExecutor.executeTransaction(
                1.bitcoin(),
                "SERVER_DEPOSIT_ADDRESS",
                fromAccount,
                Memo("MEMO_BODY", "text")
            )
        ).thenReturn(Single.error<String>(TransactionHashApiException("The transaction failed", "TX_HASH")))
        whenever(tradeExecutionService.putTradeFailureReason(any(), any(), any()))
            .thenReturn(Completable.complete())
        subject.onViewReady()
        exchangeViewSubject.onNext(mock {
            on { latestQuote } `it returns` quote
            on { this.fromAccount } `it returns` fromAccount
            on { this.toAccount } `it returns` toAccount
        })
        verify(transactionExecutor).getReceiveAddress(fromAccount)
        verify(transactionExecutor).getReceiveAddress(toAccount)
        verify(transactionExecutor).executeTransaction(any(), any(), any(), any())
        verifyNoMoreInteractions(transactionExecutor)
        verify(tradeExecutionService).executeTrade(any(), any(), any())
        verify(tradeExecutionService).putTradeFailureReason(tradeTransaction, "TX_HASH", "The transaction failed")
        verifyNoMoreInteractions(tradeExecutionService)
        verify(view, never()).continueToExchangeLocked(any())
    }

    @Test
    fun `a send transaction error trade - XLM`() {
        val fromAccount = AccountReference.Xlm("", "")
        val toAccount = AccountReference.Ethereum("", "")
        val (exchangeViewSubject, quote, tradeTransaction) =
            givenTradeExpected(fromAccount, toAccount, 1.lumens())
        val memo = Memo("MEMO_BODY", "text")
        whenever(
            transactionExecutor.executeTransaction(
                1.lumens(),
                "SERVER_DEPOSIT_ADDRESS",
                fromAccount,
                memo
            )
        ).thenReturn(
            Single.error<String>(
                SendException(
                    SendFundsResult(
                        SendDetails(
                            fromAccount,
                            1.lumens(),
                            "SERVER_DEPOSIT_ADDRESS",
                            memo
                        ),
                        99,
                        null,
                        "TX_HASH",
                        null
                    )
                )
            )
        )
        whenever(tradeExecutionService.putTradeFailureReason(any(), any(), any()))
            .thenReturn(Completable.complete())
        subject.onViewReady()
        exchangeViewSubject.onNext(mock {
            on { latestQuote } `it returns` quote
            on { this.fromAccount } `it returns` fromAccount
            on { this.toAccount } `it returns` toAccount
        })
        verify(transactionExecutor).getReceiveAddress(fromAccount)
        verify(transactionExecutor).getReceiveAddress(toAccount)
        verify(transactionExecutor).executeTransaction(any(), any(), any(), any())
        verifyNoMoreInteractions(transactionExecutor)
        verify(tradeExecutionService).executeTrade(any(), any(), any())
        verify(tradeExecutionService).putTradeFailureReason(tradeTransaction, "TX_HASH", "SendException 99")
        verifyNoMoreInteractions(tradeExecutionService)
        verify(view, never()).continueToExchangeLocked(any())
    }

    private fun givenTradeExpected(
        fromAccount: AccountReference,
        toAccount: AccountReference,
        value: CryptoValue = 1.bitcoin()
    ): Triple<PublishSubject<ExchangeViewState>, Quote, TradeTransaction> {
        val exchangeViewSubject = PublishSubject.create<ExchangeViewState>()
        val quote = Quote(
            fix = Fix.BASE_CRYPTO,
            from = Quote.Value(value, 5000.usd()),
            to = Quote.Value(50.ether(), 4900.usd()),
            baseToFiatRate = BigDecimal.ZERO,
            baseToCounterRate = BigDecimal.ZERO,
            counterToFiatRate = BigDecimal.ZERO,
            rawQuote = null
        )
        whenever(view.exchangeViewState).thenReturn(exchangeViewSubject)
        whenever(payloadDecrypt.isDoubleEncrypted).thenReturn(false)
        whenever(transactionExecutor.getReceiveAddress(fromAccount)).thenReturn(Single.just("RETURN_ADDRESS"))
        whenever(transactionExecutor.getReceiveAddress(toAccount)).thenReturn(Single.just("TO_ADDRESS"))
        val tradeTransaction = object : TradeTransaction {
            override val id = "order_id"
            override val createdAt = ""
            override val pair = quote.from.cryptoValue.currency to quote.to.cryptoValue.currency
            override val fee = quote.from.cryptoValue.currency.withMajorValue(0.001.toBigDecimal())
            override val fiatValue = quote.from.fiatValue
            override val refundAddress = "RETURN_ADDRESS"
            override val depositAddress = "SERVER_DEPOSIT_ADDRESS"
            override val depositTextMemo = "MEMO_BODY"
            override val deposit = quote.from.cryptoValue
            override val withdrawalAddress = "TO_ADDRESS"
            override val withdrawal = quote.to.cryptoValue
            override val hashOut = "HASH"
        } as TradeTransaction
        whenever(tradeExecutionService.executeTrade(quote, "TO_ADDRESS", "RETURN_ADDRESS"))
            .thenReturn(
                Single.just(
                    tradeTransaction
                )
            )
        return Triple(exchangeViewSubject, quote, tradeTransaction)
    }
}
