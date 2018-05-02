package piuk.blockchain.androidbuysellui.ui.launcher

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.junit.Before
import org.junit.Test
import piuk.blockchain.android.RxTest
import piuk.blockchain.android.ui.buysell.launcher.BuySellLauncherPresenter
import piuk.blockchain.android.ui.buysell.launcher.BuySellLauncherView

class BuySellLauncherPresenterTest: RxTest() {

    private lateinit var subject: BuySellLauncherPresenter

    private val view: BuySellLauncherView = mock()

    @Before
    fun setup() {
        subject = BuySellLauncherPresenter()
        subject.initView(view)
    }

    @Test
    fun `onViewReady`() {

        // Arrange

        // Act
        subject.onViewReady()

        // Assert
        verify(view).onStartCoinifySignUp()
    }
}