package piuk.blockchain.android.ui.send.send2

import com.blockchain.android.testutils.rxInit
import com.blockchain.testutils.lumens
import com.blockchain.testutils.stroops
import com.blockchain.testutils.usd
import com.blockchain.transactions.TransactionSender
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import info.blockchain.balance.AccountReference
import info.blockchain.balance.CryptoCurrency
import info.blockchain.balance.CryptoValue
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.TestScheduler
import org.amshove.kluent.`it returns`
import org.amshove.kluent.mock
import org.junit.Rule
import org.junit.Test
import piuk.blockchain.android.R
import piuk.blockchain.android.ui.send.SendView
import piuk.blockchain.android.ui.send.external.SendConfirmationDetails
import piuk.blockchain.androidcore.data.currency.CurrencyState
import java.util.concurrent.TimeUnit

class XlmSendPresenterStrategyTest {

    private val testScheduler = TestScheduler()

    @get:Rule
    val initSchedulers = rxInit {
        mainTrampoline()
        ioTrampoline()
        computation(testScheduler)
    }

    private fun givenXlmCurrencyState(): CurrencyState =
        mock {
            on { cryptoCurrency } `it returns` CryptoCurrency.XLM
        }

    @Test
    fun `on onCurrencySelected`() {
        val view: SendView = mock()
        XlmSendPresenterStrategy(
            givenXlmCurrencyState(),
            mock {
                on { defaultAccount() } `it returns` Single.just(
                    AccountReference.Xlm("The Xlm account", "")
                )
                on { getBalance() } `it returns` Single.just(
                    200.lumens()
                )
            },
            mock(),
            mock()
        ).apply {
            initView(view)
        }.onCurrencySelected(CryptoCurrency.XLM)
        verify(view).hideFeePriority()
        verify(view).setFeePrioritySelection(0)
        verify(view).disableFeeDropdown()
        verify(view).setCryptoMaxLength(15)
        verify(view).updateMaxAvailable(200.lumens() - 100.stroops(), CryptoValue.ZeroXlm)
        verify(view, never()).updateCryptoAmount(any())
    }

    @Test
    fun `on onSpendMaxClicked updates the CryptoAmount`() {
        val view: SendView = mock()
        XlmSendPresenterStrategy(
            givenXlmCurrencyState(),
            mock {
                on { defaultAccount() } `it returns` Single.just(
                    AccountReference.Xlm("The Xlm account", "")
                )
                on { getBalance() } `it returns` Single.just(
                    150.lumens()
                )
            },
            mock(),
            mock()
        ).apply {
            initView(view)
            onCurrencySelected(CryptoCurrency.XLM)
        }.onSpendMaxClicked()
        verify(view).updateCryptoAmount(150.lumens() - 100.stroops())
    }

    @Test
    fun `on selectDefaultOrFirstFundedSendingAccount, it updates the address`() {
        val view: SendView = mock()
        XlmSendPresenterStrategy(
            givenXlmCurrencyState(),
            mock {
                on { defaultAccount() } `it returns` Single.just(
                    AccountReference.Xlm("The Xlm account", "")
                )
            },
            mock(),
            mock()
        ).apply {
            initView(view)
        }.selectDefaultOrFirstFundedSendingAccount()
        verify(view).updateSendingAddress("The Xlm account")
    }

    @Test
    fun `on onContinueClicked, it takes the address from the view, latest value and displays the send details`() {
        val view: SendView = mock {
            on { getReceivingAddress() } `it returns` "GBAHSNSG37BOGBS4GXUPMHZWJQ22WIOJQYORRBHTABMMU6SGSKDEAOPT"
        }
        val transactionSendDataManager = mock<TransactionSender> {
            on { sendFunds(any(), any(), any()) } `it returns` Completable.timer(2, TimeUnit.SECONDS)
        }
        val xlmAccountRef = AccountReference.Xlm("The Xlm account", "")
        XlmSendPresenterStrategy(
            givenXlmCurrencyState(),
            mock {
                on { defaultAccount() } `it returns` Single.just(
                    xlmAccountRef
                )
            },
            transactionSendDataManager,
            mock {
                on { getFiat(100.lumens()) } `it returns` 50.usd()
                on { getFiat(100.stroops()) } `it returns` 0.05.usd()
            }
        ).apply {
            initView(view)
            onViewReady()
            onCryptoTextChange("1")
            onCryptoTextChange("10")
            onCryptoTextChange("100")
            onContinueClicked()
        }
        verify(view).showPaymentDetails(any())
        verify(view).showPaymentDetails(
            SendConfirmationDetails(
                from = xlmAccountRef,
                to = "GBAHSNSG37BOGBS4GXUPMHZWJQ22WIOJQYORRBHTABMMU6SGSKDEAOPT",
                amount = 100.lumens(),
                fees = 100.stroops(),
                fiatAmount = 50.usd(),
                fiatFees = 0.05.usd()
            )
        )
        verify(transactionSendDataManager, never()).sendFunds(any(), any(), any())
    }

    @Test
    fun `on submitPayment, it takes the address from the view, latest value and executes a send`() {
        val view: SendView = mock {
            on { getReceivingAddress() } `it returns` "GBAHSNSG37BOGBS4GXUPMHZWJQ22WIOJQYORRBHTABMMU6SGSKDEAOPT"
        }
        val transactionSendDataManager = mock<TransactionSender> {
            on { sendFunds(any(), any(), any()) } `it returns` Completable.timer(2, TimeUnit.SECONDS)
        }
        XlmSendPresenterStrategy(
            givenXlmCurrencyState(),
            mock {
                on { defaultAccount() } `it returns` Single.just(
                    AccountReference.Xlm("The Xlm account", "GBAHSNSG37BOGBS4GXUPMHZWJQ22WIOJQYORRBHTABMMU6SGSKDEAOPT")
                )
            },
            transactionSendDataManager,
            mock {
                on { getFiat(100.lumens()) } `it returns` 50.usd()
                on { getFiat(100.stroops()) } `it returns` 0.05.usd()
            }
        ).apply {
            initView(view)
            onViewReady()
            onCryptoTextChange("1")
            onCryptoTextChange("10")
            onCryptoTextChange("100")
            onContinueClicked()
            submitPayment()
        }
        verify(transactionSendDataManager).sendFunds(
            from = eq(
                AccountReference.Xlm(
                    "The Xlm account",
                    "GBAHSNSG37BOGBS4GXUPMHZWJQ22WIOJQYORRBHTABMMU6SGSKDEAOPT"
                )
            ),
            value = eq(100.lumens()),
            toAddress = eq("GBAHSNSG37BOGBS4GXUPMHZWJQ22WIOJQYORRBHTABMMU6SGSKDEAOPT")
        )
        verify(view).showProgressDialog(R.string.app_name)
        testScheduler.advanceTimeBy(1999, TimeUnit.MILLISECONDS)
        verify(view, never()).dismissProgressDialog()
        verify(view, never()).dismissConfirmationDialog()
        testScheduler.advanceTimeBy(1, TimeUnit.MILLISECONDS)
        verify(view).dismissProgressDialog()
        verify(view).dismissConfirmationDialog()
        verify(view).showTransactionSuccess(CryptoCurrency.XLM)
    }
}
