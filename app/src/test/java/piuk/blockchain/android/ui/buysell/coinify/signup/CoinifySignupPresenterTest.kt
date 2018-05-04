package piuk.blockchain.android.ui.buysell.coinify.signup

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Observable
import io.reactivex.Single
import org.amshove.kluent.mock
import org.junit.Before
import org.junit.Test
import piuk.blockchain.android.RxTest
import piuk.blockchain.androidbuysell.datamanagers.CoinifyDataManager
import piuk.blockchain.androidbuysell.models.CoinifyData
import piuk.blockchain.androidbuysell.models.ExchangeData
import piuk.blockchain.androidbuysell.models.coinify.KycResponse
import piuk.blockchain.androidbuysell.models.coinify.ReviewState
import piuk.blockchain.androidbuysell.models.coinify.Trader
import piuk.blockchain.androidbuysell.services.ExchangeService

class CoinifySignupPresenterTest: RxTest() {

    private lateinit var subject: CoinifySignupPresenter

    private val view: CoinifySignupView = mock()
    private val exchangeService: ExchangeService = mock()
    private val coinifyDataManager: CoinifyDataManager = mock()

    @Before
    fun setup() {
        subject = CoinifySignupPresenter(
                exchangeService,
                coinifyDataManager)
        subject.initView(view)
    }

    @Test
    fun `onViewReady error`() {

        // Arrange
        whenever(exchangeService.getExchangeMetaData()).thenReturn(Observable.error(Throwable("Forced fail")))

        // Act
        subject.onViewReady()

        // Assert
        verify(view).showToast(any())
        verify(view).onFinish()
        verifyNoMoreInteractions(view)
    }

    @Test
    fun `onViewReady no Coinify metadata`() {

        // Arrange
        val mockExchangeData: ExchangeData = mock()
        whenever(mockExchangeData.coinify).thenReturn(null)

        whenever(exchangeService.getExchangeMetaData()).thenReturn(Observable.just(mockExchangeData))

        // Act
        subject.onViewReady()

        // Assert
        verify(view).onStartWelcome()
        verifyNoMoreInteractions(view)
    }

    @Test
    fun `onViewReady no kyc responses`() {

        // Arrange
        val mockExchangeData: ExchangeData = mock()
        val mockCoinifyData: CoinifyData = mock()
        whenever(mockExchangeData.coinify).thenReturn(mockCoinifyData)
        whenever(mockCoinifyData.token).thenReturn("token")
        whenever(exchangeService.getExchangeMetaData()).thenReturn(Observable.just(mockExchangeData))

        val mockTrader: Trader = mock()
        whenever(coinifyDataManager.getTrader(any())).thenReturn(Single.just(mockTrader))

        val kycResponseList = emptyList<KycResponse>()
        whenever(coinifyDataManager.getKycReviews(any())).thenReturn(Single.just(kycResponseList))

        val kycResponse: KycResponse = mock()
        whenever(kycResponse.redirectUrl).thenReturn("this url")
        whenever(coinifyDataManager.startKycReview(any())).thenReturn(Single.just(kycResponse))

        // Act
        subject.onViewReady()

        // Assert
        verify(view).onStartVerifyIdentification("this url")
        verifyNoMoreInteractions(view)
    }

    @Test
    fun `onViewReady 1 Completed kyc`() {

        // Arrange
        val mockExchangeData: ExchangeData = mock()
        val mockCoinifyData: CoinifyData = mock()
        whenever(mockExchangeData.coinify).thenReturn(mockCoinifyData)
        whenever(mockCoinifyData.token).thenReturn("token")
        whenever(exchangeService.getExchangeMetaData()).thenReturn(Observable.just(mockExchangeData))

        val mockTrader: Trader = mock()
        whenever(coinifyDataManager.getTrader(any())).thenReturn(Single.just(mockTrader))

        val kyc1: KycResponse = mock()
        whenever(kyc1.state).thenReturn(ReviewState.Completed)
        val kycResponseList = listOf(kyc1)
        whenever(coinifyDataManager.getKycReviews(any())).thenReturn(Single.just(kycResponseList))

        // Act
        subject.onViewReady()

        // Assert
        verify(view).onStartOverview()
        verifyNoMoreInteractions(view)
    }

    @Test
    fun `onViewReady 1 Reviewing kyc`() {

        // Arrange
        val mockExchangeData: ExchangeData = mock()
        val mockCoinifyData: CoinifyData = mock()
        whenever(mockExchangeData.coinify).thenReturn(mockCoinifyData)
        whenever(mockCoinifyData.token).thenReturn("token")
        whenever(exchangeService.getExchangeMetaData()).thenReturn(Observable.just(mockExchangeData))

        val mockTrader: Trader = mock()
        whenever(coinifyDataManager.getTrader(any())).thenReturn(Single.just(mockTrader))

        val kyc1: KycResponse = mock()
        whenever(kyc1.state).thenReturn(ReviewState.Reviewing)
        val kycResponseList = listOf(kyc1)
        whenever(coinifyDataManager.getKycReviews(any())).thenReturn(Single.just(kycResponseList))

        // Act
        subject.onViewReady()

        // Assert
        verify(view).onStartOverview()
        verifyNoMoreInteractions(view)
    }

    @Test
    fun `onViewReady 1 DocumentsRequested kyc`() {

        // Arrange
        val mockExchangeData: ExchangeData = mock()
        val mockCoinifyData: CoinifyData = mock()
        whenever(mockExchangeData.coinify).thenReturn(mockCoinifyData)
        whenever(mockCoinifyData.token).thenReturn("token")
        whenever(exchangeService.getExchangeMetaData()).thenReturn(Observable.just(mockExchangeData))

        val mockTrader: Trader = mock()
        whenever(coinifyDataManager.getTrader(any())).thenReturn(Single.just(mockTrader))

        val kyc1: KycResponse = mock()
        whenever(kyc1.state).thenReturn(ReviewState.DocumentsRequested)
        whenever(kyc1.redirectUrl).thenReturn("this url")
        val kycResponseList = listOf(kyc1)
        whenever(coinifyDataManager.getKycReviews(any())).thenReturn(Single.just(kycResponseList))

        // Act
        subject.onViewReady()

        // Assert
        verify(view).onStartVerifyIdentification("this url")
        verifyNoMoreInteractions(view)
    }

    @Test
    fun `onViewReady 1 Pending kyc`() {

        // Arrange
        val mockExchangeData: ExchangeData = mock()
        val mockCoinifyData: CoinifyData = mock()
        whenever(mockExchangeData.coinify).thenReturn(mockCoinifyData)
        whenever(mockCoinifyData.token).thenReturn("token")
        whenever(exchangeService.getExchangeMetaData()).thenReturn(Observable.just(mockExchangeData))

        val mockTrader: Trader = mock()
        whenever(coinifyDataManager.getTrader(any())).thenReturn(Single.just(mockTrader))

        val kyc1: KycResponse = mock()
        whenever(kyc1.state).thenReturn(ReviewState.Pending)
        whenever(kyc1.redirectUrl).thenReturn("this url")
        val kycResponseList = listOf(kyc1)
        whenever(coinifyDataManager.getKycReviews(any())).thenReturn(Single.just(kycResponseList))

        // Act
        subject.onViewReady()

        // Assert
        verify(view).onStartVerifyIdentification("this url")
        verifyNoMoreInteractions(view)
    }

    @Test
    fun `onViewReady 1 Rejected kyc`() {

        // Arrange
        val mockExchangeData: ExchangeData = mock()
        val mockCoinifyData: CoinifyData = mock()
        whenever(mockExchangeData.coinify).thenReturn(mockCoinifyData)
        whenever(mockCoinifyData.token).thenReturn("token")
        whenever(exchangeService.getExchangeMetaData()).thenReturn(Observable.just(mockExchangeData))

        val mockTrader: Trader = mock()
        whenever(coinifyDataManager.getTrader(any())).thenReturn(Single.just(mockTrader))

        val kyc1: KycResponse = mock()
        whenever(kyc1.state).thenReturn(ReviewState.Rejected)
        val kycResponseList = listOf(kyc1)
        whenever(coinifyDataManager.getKycReviews(any())).thenReturn(Single.just(kycResponseList))

        // Act
        subject.onViewReady()

        // Assert
        verify(view).onStartWelcome()
        verifyNoMoreInteractions(view)
    }

    @Test
    fun `onViewReady 1 Completed and 1 Expired kyc`() {

        // Arrange
        val mockExchangeData: ExchangeData = mock()
        val mockCoinifyData: CoinifyData = mock()
        whenever(mockExchangeData.coinify).thenReturn(mockCoinifyData)
        whenever(mockCoinifyData.token).thenReturn("token")
        whenever(exchangeService.getExchangeMetaData()).thenReturn(Observable.just(mockExchangeData))

        val mockTrader: Trader = mock()
        whenever(coinifyDataManager.getTrader(any())).thenReturn(Single.just(mockTrader))

        val kyc1: KycResponse = mock()
        whenever(kyc1.state).thenReturn(ReviewState.Completed)
        val kyc2: KycResponse = mock()
        whenever(kyc2.state).thenReturn(ReviewState.Expired)
        val kycResponseList = listOf(kyc1, kyc2)
        whenever(coinifyDataManager.getKycReviews(any())).thenReturn(Single.just(kycResponseList))

        // Act
        subject.onViewReady()

        // Assert
        verify(view).onStartOverview()
        verifyNoMoreInteractions(view)
    }

    @Test
    fun `continueVerifyIdentification 1 Reviewing kyc`() {

        // Arrange
        val mockExchangeData: ExchangeData = mock()
        val mockCoinifyData: CoinifyData = mock()
        whenever(mockExchangeData.coinify).thenReturn(mockCoinifyData)
        whenever(mockCoinifyData.token).thenReturn("token")
        whenever(exchangeService.getExchangeMetaData()).thenReturn(Observable.just(mockExchangeData))

        val kyc1: KycResponse = mock()
        whenever(kyc1.state).thenReturn(ReviewState.Reviewing)
        val kycResponseList = listOf(kyc1)
        whenever(coinifyDataManager.getKycReviews(any())).thenReturn(Single.just(kycResponseList))

        // Act
        subject.continueVerifyIdentification()

        // Assert
        verify(view).onStartReviewInProgress()
        verifyNoMoreInteractions(view)
    }
}