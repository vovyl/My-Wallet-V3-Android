package piuk.blockchain.android.ui.buysell.coinify.signup.verify_email

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
import com.nhaarman.mockito_kotlin.whenever
import info.blockchain.wallet.api.data.Settings
import io.reactivex.Observable
import org.junit.Before
import org.junit.Test
import piuk.blockchain.android.RxTest
import piuk.blockchain.androidcore.data.settings.SettingsDataManager

class CoinifyVerifyEmailPresenterTest: RxTest() {

    private lateinit var subject: CoinifyVerifyEmailPresenter

    private val view: CoinifyVerifyEmailView = mock()
    private val settingsDataManager: SettingsDataManager = mock()

    @Before
    fun setup() {
        subject = CoinifyVerifyEmailPresenter(settingsDataManager)
        subject.initView(view)
    }

    @Test
    fun `onViewReady unverified email`() {

        // Arrange
        val email = "hello@email.com"

        var settings: Settings = mock()
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

        var settings: Settings = mock()
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

        // Arrange
        whenever(settingsDataManager.fetchSettings()).thenReturn(Observable.error(Throwable("Forced fail")))

        // Act
        subject.onViewReady()

        // Assert
        verify(view).onShowErrorAndClose()
        verifyNoMoreInteractions(view)
    }

    @Test
    fun `onContinueClicked`() {

        // Arrange
        subject.setVerifiedEmailAndDisplay("hey@email.com")

        // Act
        subject.onContinueClicked()

        // Assert
        verify(view).onShowVerifiedEmail("hey@email.com")
        verify(view).onStartCreateAccountCompleted("hey@email.com")
        verifyNoMoreInteractions(view)
    }
}