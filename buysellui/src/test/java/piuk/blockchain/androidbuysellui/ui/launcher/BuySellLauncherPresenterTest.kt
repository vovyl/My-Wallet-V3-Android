package piuk.blockchain.androidbuysellui.ui.launcher

import com.nhaarman.mockito_kotlin.atLeastOnce
import org.junit.Test

import org.junit.Before
import piuk.blockchain.androidbuysell.services.ExchangeService
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Observable
import piuk.blockchain.androidbuysell.models.CoinifyData
import piuk.blockchain.androidbuysell.models.ExchangeData
import piuk.blockchain.androidbuysellui.RxTest

class BuySellLauncherPresenterTest: RxTest() {

    private lateinit var subject: BuySellLauncherPresenter

    private val view: BuySellLauncherView = mock()
    private var exchangeService: ExchangeService = mock()

    @Before
    fun setup() {
        subject = BuySellLauncherPresenter(exchangeService)
        subject.initView(view)
    }

    @Test
    fun `onViewReady has coinify token`() {

        // Arrange
        val exchange: ExchangeData = mock()
        val coinify: CoinifyData = mock()
        whenever(coinify.token).thenReturn("someToken")
        whenever(exchange.coinify).thenReturn(coinify)

        whenever(exchangeService.getExchangeMetaData()).thenReturn(Observable.just(exchange))

        // Act
        subject.onViewReady()

        // Assert
        verify(exchangeService, atLeastOnce()).getExchangeMetaData()
        verify(view).onStartOverview()
    }

    @Test
    fun `onViewReady has exchange data but no coinify token`() {

        // Arrange
        val exchange: ExchangeData = mock()
        val coinify: CoinifyData = mock()
        whenever(coinify.token).thenReturn(null)
        whenever(exchange.coinify).thenReturn(coinify)

        whenever(exchangeService.getExchangeMetaData()).thenReturn(Observable.just(exchange))

        // Act
        subject.onViewReady()

        // Assert
        verify(exchangeService, atLeastOnce()).getExchangeMetaData()
        verify(view).onStartSignup()
    }

    @Test
    fun `onViewReady no exchange data`() {

        // Arrange
        whenever(exchangeService.getExchangeMetaData()).thenReturn(Observable.just(ExchangeData()))

        // Act
        subject.onViewReady()

        // Assert
        verify(exchangeService, atLeastOnce()).getExchangeMetaData()
        verify(view).onStartSignup()
    }

    @Test
    fun coinifySignupRequiredObservable() {
    }
}