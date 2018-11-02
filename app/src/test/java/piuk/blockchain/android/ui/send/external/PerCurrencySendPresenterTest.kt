package piuk.blockchain.android.ui.send.external

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import info.blockchain.balance.CryptoCurrency
import io.reactivex.Completable
import org.amshove.kluent.`it returns`
import org.amshove.kluent.mock
import org.junit.Test
import piuk.blockchain.android.ui.send.SendView
import piuk.blockchain.androidcore.data.currency.CurrencyState

class PerCurrencySendPresenterTest {

    @Test
    fun `handles xlm address scan, delegates to xlm strategy`() {
        val view: SendView = mock()
        val xlmStrategy: SendPresenter<SendView> = mock()
        val currencyState = mock<CurrencyState> {
            on { cryptoCurrency } `it returns` CryptoCurrency.XLM
        }
        PerCurrencySendPresenter(
            mock(),
            xlmStrategy,
            currencyState,
            mock(),
            mock(),
            mock {
                on { updateTickers() } `it returns` Completable.complete()
            }
        ).apply {
            initView(view)
            handleURIScan("GDYULVJK2T6G7HFUC76LIBKZEMXPKGINSG6566EPWJKCLXTYVWJ7XPY4")
        }
        verify(currencyState).cryptoCurrency = CryptoCurrency.XLM
        verify(xlmStrategy).handleURIScan("GDYULVJK2T6G7HFUC76LIBKZEMXPKGINSG6566EPWJKCLXTYVWJ7XPY4")
        verify(xlmStrategy).onCurrencySelected(CryptoCurrency.XLM)
        verify(view).setSelectedCurrency(CryptoCurrency.XLM)
    }

    @Test
    fun `handles btc address scan, delegates to original strategy`() {
        val view: SendView = mock()
        val originalStrategy: SendPresenter<SendView> = mock()
        PerCurrencySendPresenter(
            originalStrategy,
            mock(),
            mock(),
            mock(),
            mock(),
            mock {
                on { updateTickers() } `it returns` Completable.complete()
            }
        ).apply {
            initView(view)
            handleURIScan("1FBPzxps6kGyk2exqLvz7cRMi2odtLEVQ")
        }
        verify(originalStrategy).handleURIScan("1FBPzxps6kGyk2exqLvz7cRMi2odtLEVQ")
    }
}