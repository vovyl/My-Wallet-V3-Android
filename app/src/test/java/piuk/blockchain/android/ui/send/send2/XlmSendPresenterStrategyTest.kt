package piuk.blockchain.android.ui.send.send2

import com.blockchain.android.testutils.rxInit
import com.blockchain.testutils.lumens
import com.blockchain.testutils.stroops
import com.blockchain.testutils.usd
import com.blockchain.transactions.Memo
import com.blockchain.transactions.SendDetails
import com.blockchain.transactions.SendFundsResult
import com.blockchain.transactions.TransactionSender
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyZeroInteractions
import info.blockchain.balance.AccountReference
import info.blockchain.balance.CryptoCurrency
import info.blockchain.balance.CryptoValue
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.TestScheduler
import org.amshove.kluent.`it returns`
import org.amshove.kluent.`should be`
import org.amshove.kluent.mock
import org.junit.Rule
import org.junit.Test
import piuk.blockchain.android.R
import piuk.blockchain.android.ui.send.SendView
import piuk.blockchain.android.ui.send.external.SendConfirmationDetails
import piuk.blockchain.androidcore.data.currency.CurrencyState
import piuk.blockchain.androidcore.data.exchangerate.FiatExchangeRates
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
        val view = TestSendView()
        XlmSendPresenterStrategy(
            givenXlmCurrencyState(),
            mock {
                on { defaultAccount() } `it returns` Single.just(
                    AccountReference.Xlm("The Xlm account", "")
                )
                on { getMaxSpendableAfterFees() } `it returns` Single.just(
                    199.5.lumens()
                )
            },
            mock(),
            mock(),
            mock()
        ).apply {
            initView(view)
        }.onCurrencySelected(CryptoCurrency.XLM)
        verify(view.mock).hideFeePriority()
        verify(view.mock).setFeePrioritySelection(0)
        verify(view.mock).disableFeeDropdown()
        verify(view.mock).setCryptoMaxLength(15)
        verify(view.mock).showMemo()
        verify(view.mock).updateMaxAvailable(199.5.lumens(), CryptoValue.ZeroXlm)
        verify(view.mock, never()).updateCryptoAmount(any())
    }

    @Test
    fun `on onSpendMaxClicked updates the CryptoAmount`() {
        val view = TestSendView()
        XlmSendPresenterStrategy(
            givenXlmCurrencyState(),
            mock {
                on { defaultAccount() } `it returns` Single.just(
                    AccountReference.Xlm("The Xlm account", "")
                )
                on { getMaxSpendableAfterFees() } `it returns` Single.just(
                    150.lumens()
                )
            },
            mock(),
            mock(),
            mock()
        ).apply {
            initView(view)
            onCurrencySelected(CryptoCurrency.XLM)
        }.onSpendMaxClicked()
        verify(view.mock).updateCryptoAmount(150.lumens())
    }

    @Test
    fun `on selectDefaultOrFirstFundedSendingAccount, it updates the address`() {
        val view = TestSendView()
        val fiatExchangeRates = mock<FiatExchangeRates>()
        XlmSendPresenterStrategy(
            givenXlmCurrencyState(),
            mock {
                on { defaultAccount() } `it returns` Single.just(
                    AccountReference.Xlm("The Xlm account", "")
                )
                on { fees() } `it returns` 99.stroops()
            },
            mock(),
            fiatExchangeRates,
            mock()
        ).apply {
            initView(view)
        }.selectDefaultOrFirstFundedSendingAccount()
        verify(view.mock).updateSendingAddress("The Xlm account")
        verify(view.mock).updateFeeAmount(99.stroops(), fiatExchangeRates)
    }

    @Test
    fun `on onContinueClicked, it takes the address from the view, latest value and displays the send details`() {
        val view = TestSendView()
        val result = SendFundsResult(
            errorCode = 0,
            confirmationDetails = null,
            hash = "TX_HASH",
            sendDetails = mock()
        )
        val transactionSendDataManager = mock<TransactionSender> {
            on { dryRunSendFunds(any()) } `it returns` Single.just(result)
            on { sendFunds(any()) } `it returns` Completable.timer(2, TimeUnit.SECONDS)
                .andThen(
                    Single.just(
                        result
                    )
                )
        }
        val xlmAccountRef = AccountReference.Xlm("The Xlm account", "")
        XlmSendPresenterStrategy(
            givenXlmCurrencyState(),
            mock {
                on { defaultAccount() } `it returns` Single.just(
                    xlmAccountRef
                )
                on { getMaxSpendableAfterFees() } `it returns` Single.just(
                    99.lumens()
                )
                on { fees() } `it returns` 200.stroops()
            },
            transactionSendDataManager,
            mock {
                on { getFiat(100.lumens()) } `it returns` 50.usd()
                on { getFiat(200.stroops()) } `it returns` 0.05.usd()
            },
            mock()
        ).apply {
            initView(view)
            onViewReady()
            view.assertSendButtonDisabled()
            onAddressTextChange("GBAHSNSG37BOGBS4GXUPMHZWJQ22WIOJQYORRBHTABMMU6SGSKDEAOPT")
            onCryptoTextChange("1")
            onCryptoTextChange("10")
            onCryptoTextChange("100")
            testScheduler.advanceTimeBy(200, TimeUnit.MILLISECONDS)
            view.assertSendButtonEnabled()
            onContinueClicked()
        }
        verify(view.mock).showPaymentDetails(any())
        verify(view.mock).showPaymentDetails(
            SendConfirmationDetails(
                SendDetails(
                    from = xlmAccountRef,
                    toAddress = "GBAHSNSG37BOGBS4GXUPMHZWJQ22WIOJQYORRBHTABMMU6SGSKDEAOPT",
                    value = 100.lumens(),
                    memo = Memo.None
                ),
                fees = 200.stroops(),
                fiatAmount = 50.usd(),
                fiatFees = 0.05.usd()
            )
        )
        verify(transactionSendDataManager, never()).sendFunds(any())
    }

    @Test
    fun `a dry run happens during field entry and disables send`() {
        val view = TestSendView()
        val result = SendFundsResult(
            errorCode = 2,
            confirmationDetails = null,
            hash = "TX_HASH",
            sendDetails = mock()
        )
        val transactionSendDataManager = mock<TransactionSender> {
            on { dryRunSendFunds(any()) } `it returns` Single.just(result)
        }
        val xlmAccountRef = AccountReference.Xlm("The Xlm account", "")
        XlmSendPresenterStrategy(
            givenXlmCurrencyState(),
            mock {
                on { defaultAccount() } `it returns` Single.just(
                    xlmAccountRef
                )
                on { getMaxSpendableAfterFees() } `it returns` Single.just(
                    99.lumens()
                )
                on { fees() } `it returns` 200.stroops()
            },
            transactionSendDataManager,
            mock(),
            mock {
                on { localize(result) } `it returns` "The warning"
            }
        ).apply {
            initView(view)
            onViewReady()
            view.assertSendButtonDisabled()
            onCryptoTextChange("1")
            onCryptoTextChange("10")
            onCryptoTextChange("100")
            onAddressTextChange("GBAHSNSG37BOGBS4GXUPMHZWJQ22WIOJQYORRBHTABMMU6SGSKDEAOPT")
        }
        verify(transactionSendDataManager, never()).dryRunSendFunds(any())
        testScheduler.advanceTimeBy(200, TimeUnit.MILLISECONDS)
        verify(transactionSendDataManager).dryRunSendFunds(any())
        verify(view.mock).updateWarning("The warning")
        view.assertSendButtonDisabled()
        verify(transactionSendDataManager, never()).sendFunds(any())
    }

    @Test
    fun `when the address is empty, do not show any warning`() {
        val view = TestSendView()
        val result = SendFundsResult(
            errorCode = 2,
            confirmationDetails = null,
            hash = "TX_HASH",
            sendDetails = mock()
        )
        val transactionSendDataManager = mock<TransactionSender> {
            on { dryRunSendFunds(any()) } `it returns` Single.just(result)
        }
        val xlmAccountRef = AccountReference.Xlm("The Xlm account", "")
        XlmSendPresenterStrategy(
            givenXlmCurrencyState(),
            mock {
                on { defaultAccount() } `it returns` Single.just(
                    xlmAccountRef
                )
                on { getMaxSpendableAfterFees() } `it returns` Single.just(
                    99.lumens()
                )
                on { fees() } `it returns` 200.stroops()
            },
            transactionSendDataManager,
            mock(),
            mock {
                on { localize(result) } `it returns` "The warning"
            }
        ).apply {
            initView(view)
            onViewReady()
            view.assertSendButtonDisabled()
            onCryptoTextChange("1")
            onCryptoTextChange("10")
            onCryptoTextChange("100")
            onAddressTextChange("GBAHSNSG37BOGBS4GXUPMHZWJQ22WIOJQYORRBHTABMMU6SGSKDEAOPT")
            onAddressTextChange("")
        }
        verify(transactionSendDataManager, never()).dryRunSendFunds(any())
        testScheduler.advanceTimeBy(200, TimeUnit.MILLISECONDS)
        verify(transactionSendDataManager).dryRunSendFunds(any())
        verify(view.mock, never()).updateWarning("The warning")
        verify(view.mock).clearWarning()
        view.assertSendButtonDisabled()
        verify(transactionSendDataManager, never()).sendFunds(any())
    }

    @Test
    fun `multiple dry runs when spread out - debounce behaviour test`() {
        val view = TestSendView()
        val result = SendFundsResult(
            errorCode = 2,
            confirmationDetails = null,
            hash = "TX_HASH",
            sendDetails = mock()
        )
        val transactionSendDataManager = mock<TransactionSender> {
            on { dryRunSendFunds(any()) } `it returns` Single.just(result)
        }
        val xlmAccountRef = AccountReference.Xlm("The Xlm account", "")
        XlmSendPresenterStrategy(
            givenXlmCurrencyState(),
            mock {
                on { defaultAccount() } `it returns` Single.just(
                    xlmAccountRef
                )
                on { getMaxSpendableAfterFees() } `it returns` Single.just(
                    99.lumens()
                )
                on { fees() } `it returns` 200.stroops()
            },
            transactionSendDataManager,
            mock(),
            mock {
                on { localize(result) } `it returns` "The warning"
            }
        ).apply {
            initView(view)
            onViewReady()
            view.assertSendButtonDisabled()
            onAddressTextChange("GBAHSNSG37BOGBS4GXUPMHZWJQ22WIOJQYORRBHTABMMU6SGSKDEAOPT")
            onCryptoTextChange("1")
            testScheduler.advanceTimeBy(200, TimeUnit.MILLISECONDS)
            onCryptoTextChange("10")
            testScheduler.advanceTimeBy(200, TimeUnit.MILLISECONDS)
            onCryptoTextChange("100")
            testScheduler.advanceTimeBy(200, TimeUnit.MILLISECONDS)
        }
        verify(transactionSendDataManager, times(3)).dryRunSendFunds(any())
        verify(view.mock, times(3)).updateWarning("The warning")
        verify(view.mock, times(3)).updateWarning("The warning")
        view.assertSendButtonDisabled()
        verify(transactionSendDataManager, never()).sendFunds(any())
    }

    @Test
    fun `a successful dry run clears the warning and enables send`() {
        val view = TestSendView()
        val result = SendFundsResult(
            errorCode = 0,
            confirmationDetails = null,
            hash = "TX_HASH",
            sendDetails = mock()
        )
        val transactionSendDataManager = mock<TransactionSender> {
            on { dryRunSendFunds(any()) } `it returns` Single.just(result)
        }
        val xlmAccountRef = AccountReference.Xlm("The Xlm account", "")
        XlmSendPresenterStrategy(
            givenXlmCurrencyState(),
            mock {
                on { defaultAccount() } `it returns` Single.just(
                    xlmAccountRef
                )
                on { getMaxSpendableAfterFees() } `it returns` Single.just(
                    99.lumens()
                )
                on { fees() } `it returns` 200.stroops()
            },
            transactionSendDataManager,
            mock(),
            mock {
                on { localize(result) } `it returns` "The warning"
            }
        ).apply {
            initView(view)
            onViewReady()
            view.assertSendButtonDisabled()
            onCryptoTextChange("1")
            onAddressTextChange("GBAHSNSG37BOGBS4GXUPMHZWJQ22WIOJQYORRBHTABMMU6SGSKDEAOPT")
        }
        testScheduler.advanceTimeBy(200, TimeUnit.MILLISECONDS)
        verify(view.mock).clearWarning()
        view.assertSendButtonEnabled()
        verify(transactionSendDataManager, never()).sendFunds(any())
    }

    @Test
    fun `on submitPayment, it takes the address from the view, latest value and executes a send`() {
        val sendDetails = SendDetails(
            from = AccountReference.Xlm(
                "The Xlm account",
                "GBAHSNSG37BOGBS4GXUPMHZWJQ22WIOJQYORRBHTABMMU6SGSKDEAOPT"
            ),
            value = 100.lumens(),
            toAddress = "GBAHSNSG37BOGBS4GXUPMHZWJQ22WIOJQYORRBHTABMMU6SGSKDEAOPT",
            memo = Memo.None
        )
        val view = TestSendView()
        val result = SendFundsResult(
            errorCode = 0,
            confirmationDetails = null,
            hash = "TX_HASH",
            sendDetails = sendDetails
        )
        val transactionSendDataManager = mock<TransactionSender> {
            on { sendFunds(any()) } `it returns` Completable.timer(2, TimeUnit.SECONDS)
                .andThen(
                    Single.just(
                        result
                    )
                )
            on { dryRunSendFunds(any()) } `it returns` Single.just(result)
        }
        XlmSendPresenterStrategy(
            givenXlmCurrencyState(),
            mock {
                on { defaultAccount() } `it returns` Single.just(
                    AccountReference.Xlm("The Xlm account", "GBAHSNSG37BOGBS4GXUPMHZWJQ22WIOJQYORRBHTABMMU6SGSKDEAOPT")
                )
                on { getMaxSpendableAfterFees() } `it returns` Single.just(
                    200.lumens()
                )
                on { fees() } `it returns` 150.stroops()
            },
            transactionSendDataManager,
            mock {
                on { getFiat(100.lumens()) } `it returns` 50.usd()
                on { getFiat(150.stroops()) } `it returns` 0.05.usd()
            },
            mock()
        ).apply {
            initView(view)
            onViewReady()
            view.assertSendButtonDisabled()
            onAddressTextChange("GBAHSNSG37BOGBS4G")
            onCryptoTextChange("1")
            onCryptoTextChange("10")
            onCryptoTextChange("100")
            onAddressTextChange("GBAHSNSG37BOGBS4GXUPMHZWJQ22WIOJQYORRBHTABMMU6SGSKDEAOPT")
            testScheduler.advanceTimeBy(200, TimeUnit.MILLISECONDS)
            view.assertSendButtonEnabled()
            onContinueClicked()
            verify(transactionSendDataManager, never()).sendFunds(sendDetails)
            submitPayment()
        }
        verify(transactionSendDataManager).sendFunds(
            sendDetails
        )
        verify(view.mock).showProgressDialog(R.string.app_name)
        testScheduler.advanceTimeBy(1999, TimeUnit.MILLISECONDS)
        verify(view.mock, never()).dismissProgressDialog()
        verify(view.mock, never()).dismissConfirmationDialog()
        testScheduler.advanceTimeBy(1, TimeUnit.MILLISECONDS)
        verify(view.mock).dismissProgressDialog()
        verify(view.mock).dismissConfirmationDialog()
        verify(view.mock).showTransactionSuccess(CryptoCurrency.XLM)
    }

    @Test
    fun `on submitPayment failure`() {
        val view = TestSendView()
        val transactionSendDataManager = mock<TransactionSender> {
            on { sendFunds(any()) } `it returns` Single.error(Exception("Failure"))
            on { dryRunSendFunds(any()) } `it returns` Single.error(Exception("Failure"))
        }
        XlmSendPresenterStrategy(
            givenXlmCurrencyState(),
            mock {
                on { defaultAccount() } `it returns` Single.just(
                    AccountReference.Xlm("The Xlm account", "GBAHSNSG37BOGBS4GXUPMHZWJQ22WIOJQYORRBHTABMMU6SGSKDEAOPT")
                )
                on { getMaxSpendableAfterFees() } `it returns` Single.just(
                    200.lumens()
                )
                on { fees() } `it returns` 150.stroops()
            },
            transactionSendDataManager,
            mock {
                on { getFiat(100.lumens()) } `it returns` 50.usd()
                on { getFiat(150.stroops()) } `it returns` 0.05.usd()
            },
            mock()
        ).apply {
            initView(view)
            onViewReady()
            onAddressTextChange("GBAHSNSG37BOGBS4GXUPMHZWJQ22WIOJQYORRBHTABMMU6SGSKDEAOPT")
            onCryptoTextChange("1")
            onCryptoTextChange("10")
            onCryptoTextChange("100")
            onContinueClicked()
            submitPayment()
        }
        verify(transactionSendDataManager).sendFunds(
            SendDetails(
                from = AccountReference.Xlm(
                    "The Xlm account",
                    "GBAHSNSG37BOGBS4GXUPMHZWJQ22WIOJQYORRBHTABMMU6SGSKDEAOPT"
                ),
                value = 100.lumens(),
                toAddress = "GBAHSNSG37BOGBS4GXUPMHZWJQ22WIOJQYORRBHTABMMU6SGSKDEAOPT",
                memo = Memo.None
            )
        )
        verify(view.mock).showProgressDialog(R.string.app_name)
        verify(view.mock).dismissProgressDialog()
        verify(view.mock).dismissConfirmationDialog()
        verify(view.mock, never()).showTransactionSuccess(CryptoCurrency.XLM)
    }

    @Test
    fun `handle address scan, data is null`() {
        val view = TestSendView()
        XlmSendPresenterStrategy(
            givenXlmCurrencyState(),
            mock {
                on { defaultAccount() } `it returns` Single.just(
                    AccountReference.Xlm("The Xlm account", "GBAHSNSG37BOGBS4GXUPMHZWJQ22WIOJQYORRBHTABMMU6SGSKDEAOPT")
                )
            },
            mock(),
            mock(),
            mock()
        ).apply {
            initView(view)
            handleURIScan(null)
        }
        verifyZeroInteractions(view.mock)
    }

    @Test
    fun `handle address scan valid address`() {
        val view = TestSendView()
        XlmSendPresenterStrategy(
            givenXlmCurrencyState(),
            mock {
                on { defaultAccount() } `it returns` Single.just(
                    AccountReference.Xlm("The Xlm account", "GBAHSNSG37BOGBS4GXUPMHZWJQ22WIOJQYORRBHTABMMU6SGSKDEAOPT")
                )
            },
            mock(),
            mock {
                on { getFiat(0.lumens()) } `it returns` 0.usd()
            },
            mock()
        ).apply {
            initView(view)
            handleURIScan("GDYULVJK2T6G7HFUC76LIBKZEMXPKGINSG6566EPWJKCLXTYVWJ7XPY4")
        }
        verify(view.mock).updateCryptoAmount(0.lumens())
        verify(view.mock).updateFiatAmount(0.usd())
        verify(view.mock).updateReceivingAddress("GDYULVJK2T6G7HFUC76LIBKZEMXPKGINSG6566EPWJKCLXTYVWJ7XPY4")
    }

    @Test
    fun `handle address scan valid uri`() {
        val view = TestSendView()
        XlmSendPresenterStrategy(
            givenXlmCurrencyState(),
            mock {
                on { defaultAccount() } `it returns` Single.just(
                    AccountReference.Xlm("The Xlm account", "GBAHSNSG37BOGBS4GXUPMHZWJQ22WIOJQYORRBHTABMMU6SGSKDEAOPT")
                )
            },
            mock(),
            mock {
                on { getFiat(120.1234567.lumens()) } `it returns` 50.usd()
            },
            mock()
        ).apply {
            initView(view)
            handleURIScan(
                "web+stellar:pay?destination=" +
                    "GCALNQQBXAPZ2WIRSDDBMSTAKCUH5SG6U76YBFLQLIXJTF7FE5AX7AOO&amount=" +
                    "120.1234567&memo=skdjfasf&msg=pay%20me%20with%20lumens"
            )
        }
        verify(view.mock).updateCryptoAmount(120.1234567.lumens())
        verify(view.mock).updateFiatAmount(50.usd())
        verify(view.mock).updateReceivingAddress("GCALNQQBXAPZ2WIRSDDBMSTAKCUH5SG6U76YBFLQLIXJTF7FE5AX7AOO")
    }

    @Test
    fun `scan address, returns confirmation details`() {
        val view = TestSendView()
        val result = SendFundsResult(
            errorCode = 0,
            confirmationDetails = null,
            hash = "TX_HASH",
            sendDetails = mock()
        )
        val transactionSendDataManager = mock<TransactionSender> {
            on { dryRunSendFunds(any()) } `it returns` Single.just(result)
            on { sendFunds(any()) } `it returns` Completable.timer(2, TimeUnit.SECONDS)
                .andThen(
                    Single.just(
                        result
                    )
                )
        }
        val xlmAccountRef = AccountReference.Xlm("The Xlm account", "")
        XlmSendPresenterStrategy(
            givenXlmCurrencyState(),
            mock {
                on { defaultAccount() } `it returns` Single.just(
                    xlmAccountRef
                )
                on { getMaxSpendableAfterFees() } `it returns` Single.just(
                    99.lumens()
                )
                on { fees() } `it returns` 200.stroops()
            },
            transactionSendDataManager,
            mock {
                on { getFiat(120.1234567.lumens()) } `it returns` 99.usd()
                on { getFiat(200.stroops()) } `it returns` 0.05.usd()
            },
            mock()
        ).apply {
            initView(view)
            handleURIScan(
                "web+stellar:pay?destination=" +
                    "GCALNQQBXAPZ2WIRSDDBMSTAKCUH5SG6U76YBFLQLIXJTF7FE5AX7AOO&amount=" +
                    "120.1234567&memo=1234&memo_type=MEMO_ID&msg=pay%20me%20with%20lumens"
            )
            verify(view.mock).displayMemo(Memo("1234", type = "id"))
            onViewReady()
            view.assertSendButtonDisabled()
            testScheduler.advanceTimeBy(200, TimeUnit.MILLISECONDS)
            view.assertSendButtonEnabled()
            onContinueClicked()
        }
        verify(view.mock).showPaymentDetails(any())
        verify(view.mock).showPaymentDetails(
            SendConfirmationDetails(
                SendDetails(
                    from = xlmAccountRef,
                    toAddress = "GCALNQQBXAPZ2WIRSDDBMSTAKCUH5SG6U76YBFLQLIXJTF7FE5AX7AOO",
                    value = 120.1234567.lumens(),
                    memo = Memo("1234", type = "id")
                ),
                fees = 200.stroops(),
                fiatAmount = 99.usd(),
                fiatFees = 0.05.usd()
            )
        )
        verify(transactionSendDataManager, never()).sendFunds(any())
    }

    @Test
    fun `text memo`() {
        val memo = Memo(value = "This is the memo", type = "text")
        val sendDetails = SendDetails(
            from = AccountReference.Xlm(
                "The Xlm account",
                "GBAHSNSG37BOGBS4GXUPMHZWJQ22WIOJQYORRBHTABMMU6SGSKDEAOPT"
            ),
            value = 100.lumens(),
            toAddress = "GBAHSNSG37BOGBS4GXUPMHZWJQ22WIOJQYORRBHTABMMU6SGSKDEAOPT",
            memo = memo
        )
        val view = TestSendView()
        val result = SendFundsResult(
            errorCode = 0,
            confirmationDetails = null,
            hash = "TX_HASH",
            sendDetails = sendDetails
        )
        val transactionSendDataManager = mock<TransactionSender> {
            on { sendFunds(any()) } `it returns` Completable.timer(2, TimeUnit.SECONDS)
                .andThen(
                    Single.just(
                        result
                    )
                )
            on { dryRunSendFunds(any()) } `it returns` Single.just(result)
        }
        XlmSendPresenterStrategy(
            givenXlmCurrencyState(),
            mock {
                on { defaultAccount() } `it returns` Single.just(
                    AccountReference.Xlm("The Xlm account", "GBAHSNSG37BOGBS4GXUPMHZWJQ22WIOJQYORRBHTABMMU6SGSKDEAOPT")
                )
                on { getMaxSpendableAfterFees() } `it returns` Single.just(
                    200.lumens()
                )
                on { fees() } `it returns` 150.stroops()
            },
            transactionSendDataManager,
            mock {
                on { getFiat(100.lumens()) } `it returns` 50.usd()
                on { getFiat(150.stroops()) } `it returns` 0.05.usd()
            },
            mock()
        ).apply {
            initView(view)
            onViewReady()
            view.assertSendButtonDisabled()
            onAddressTextChange("GBAHSNSG37BOGBS4GXUPMHZWJQ22WIOJQYORRBHTABMMU6SGSKDEAOPT")
            onCryptoTextChange("100")
            onMemoChange(memo)
            testScheduler.advanceTimeBy(200, TimeUnit.MILLISECONDS)
            view.assertSendButtonEnabled()
            onContinueClicked()
            verify(transactionSendDataManager, never()).sendFunds(sendDetails)
            submitPayment()
        }
        verify(transactionSendDataManager).sendFunds(
            sendDetails
        )
        verify(view.mock).showProgressDialog(R.string.app_name)
        testScheduler.advanceTimeBy(1999, TimeUnit.MILLISECONDS)
        verify(view.mock, never()).dismissProgressDialog()
        verify(view.mock, never()).dismissConfirmationDialog()
        testScheduler.advanceTimeBy(1, TimeUnit.MILLISECONDS)
        verify(view.mock).dismissProgressDialog()
        verify(view.mock).dismissConfirmationDialog()
        verify(view.mock).showTransactionSuccess(CryptoCurrency.XLM)
    }
}

class TestSendView(val mock: SendView = mock()) : SendView by mock {

    private var sendEnabled = true

    override fun setSendButtonEnabled(enabled: Boolean) {
        sendEnabled = enabled
        mock.setSendButtonEnabled(enabled)
    }

    fun assertSendButtonEnabled() {
        sendEnabled `should be` true
    }

    fun assertSendButtonDisabled() {
        sendEnabled `should be` false
    }
}
