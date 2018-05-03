package piuk.blockchain.android.ui.buysell.coinify.signup.verify_email

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
import com.nhaarman.mockito_kotlin.whenever
import info.blockchain.wallet.api.data.Settings
import io.reactivex.Completable
import io.reactivex.Observable
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import piuk.blockchain.android.RxTest
import piuk.blockchain.androidbuysell.datamanagers.CoinifyDataManager
import piuk.blockchain.androidbuysell.models.ExchangeData
import piuk.blockchain.androidbuysell.models.coinify.KycResponse
import piuk.blockchain.androidbuysell.models.coinify.Trader
import piuk.blockchain.androidbuysell.models.coinify.TraderResponse
import piuk.blockchain.androidbuysell.services.ExchangeService
import piuk.blockchain.androidcore.data.currency.CurrencyState
import piuk.blockchain.androidcore.data.metadata.MetadataManager
import piuk.blockchain.androidcore.data.payload.PayloadDataManager
import piuk.blockchain.androidcore.data.settings.SettingsDataManager
import piuk.blockchain.androidcore.data.walletoptions.WalletOptionsDataManager

class CoinifyVerifyEmailPresenterTest: RxTest() {

    private lateinit var subject: CoinifyVerifyEmailPresenter

    private val view: CoinifyVerifyEmailView = mock()
    private val settingsDataManager: SettingsDataManager = mock()
    private val walletOptionsDataManager: WalletOptionsDataManager = mock()
    private val payloadDataManager: PayloadDataManager = mock()
    private val exchangeService: ExchangeService = mock()
    private val coinifyDataManager: CoinifyDataManager = mock()
    private val metadataManager: MetadataManager = mock()
    private val currencyState: CurrencyState = mock()

    @Before
    fun setup() {
        subject = CoinifyVerifyEmailPresenter(settingsDataManager,
                walletOptionsDataManager,
                payloadDataManager,
                exchangeService,
                coinifyDataManager,
                metadataManager,
                currencyState)
        subject.initView(view)
    }

    @Test
    fun `onViewReady unverified email`() {

        // Arrange
        val email = "hello@email.com"

        val settings: Settings = mock()
        whenever(settings.isEmailVerified).thenReturn(false)
        whenever(settings.email).thenReturn(email)
        whenever(settingsDataManager.fetchSettings()).thenReturn(Observable.just(settings))

        // Act
        subject.onViewReady()

        // Assert
        verify(view).onEnableContinueButton(false)
        verify(view).onShowUnverifiedEmail(email)
        verify(settingsDataManager).updateEmail(email)
        verifyNoMoreInteractions(view)
    }

    @Test
    fun `onViewReady verified email`() {

        // Arrange
        val email = "hello@email.com"

        val settings: Settings = mock()
        whenever(settings.isEmailVerified).thenReturn(true)
        whenever(settings.email).thenReturn(email)
        whenever(settingsDataManager.fetchSettings()).thenReturn(Observable.just(settings))

        // Act
        subject.onViewReady()

        // Assert
        verify(view).onEnableContinueButton(true)
        verify(view).onShowVerifiedEmail(email)
        verifyNoMoreInteractions(view)
    }

    @Test
    fun `onViewReady unexpected error`() {
        // FIXME: This test doesn't actually complete, it throws OnErrorNotImplementedException before reaching the assertions
        // Arrange
        whenever(settingsDataManager.fetchSettings()).thenReturn(Observable.error(Throwable("Forced fail")))

        // Act
        subject.onViewReady()

        // Assert
        verify(view).onShowErrorAndClose()
        verifyNoMoreInteractions(view)
    }

    @Test
    fun `onContinueClicked no verified email`() {

        // Arrange

        // Act
        subject.onContinueClicked("UK")

        // Assert
        verify(view).onShowErrorAndClose()
        verifyNoMoreInteractions(view)
    }

    @Ignore
    @Test
    fun `onContinueClicked`() {

        //FIXME Not sure why this isn't working yet

        // Arrange
        subject.setVerifiedEmailAndDisplay("hey@email.com")
        whenever(walletOptionsDataManager.getCoinifyPartnerId()).thenReturn(Observable.just(123))
        whenever(currencyState.fiatUnit).thenReturn("GBP")

        val mockTrader: Trader = mock()
        val mockExchangeData: ExchangeData = mock()
        val mockKycResponse: KycResponse = mock()

        whenever(coinifyDataManager.getEmailTokenAndSignUp(any(), any(),
                any(), any(),
                any(), any())).thenReturn(
                Observable.just(TraderResponse(mockTrader, "token"))
                        .singleOrError())
        whenever(exchangeService.getExchangeMetaData())
                .thenReturn(Observable.just(mockExchangeData))
        whenever(metadataManager.saveToMetadata(any(), any()))
                .thenReturn(Completable.complete())
        whenever(coinifyDataManager.startKycReview(any()))
                .thenReturn(Observable.just(mockKycResponse).single(mockKycResponse))

        // Act
        subject.onContinueClicked("UK")

        // Assert
        verify(view).onShowVerifiedEmail("hey@email.com")
        verify(view).onStartSignUpSuccess()
        verifyNoMoreInteractions(view)
    }
}