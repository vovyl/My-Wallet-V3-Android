package piuk.blockchain.androidcore.data.fees

import com.blockchain.android.testutils.rxInit
import com.nhaarman.mockito_kotlin.whenever
import info.blockchain.wallet.api.Environment
import info.blockchain.wallet.api.FeeApi
import io.reactivex.Observable
import io.reactivex.Single
import org.amshove.kluent.`should equal to`
import org.amshove.kluent.mock
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import piuk.blockchain.androidcore.data.api.EnvironmentConfig
import piuk.blockchain.androidcore.data.rxjava.RxBus
import piuk.blockchain.androidcore.data.walletoptions.WalletOptionsDataManager

class FeeDataManagerTest {

    private lateinit var subject: FeeDataManager
    private val rxBus = RxBus()
    private val feeApi: FeeApi = mock()
    private val environmentSettings: EnvironmentConfig = mock()
    private val walletOptionsDataManager: WalletOptionsDataManager = mock()

    @Suppress("unused")
    @get:Rule
    val initSchedulers = rxInit {
        mainTrampoline()
        ioTrampoline()
    }

    @Before
    fun setUp() {
        subject = FeeDataManager(feeApi, walletOptionsDataManager, environmentSettings, rxBus)
    }

    @Test
    fun getBchFeeOptions() {
        whenever(walletOptionsDataManager.getBchFee()).thenReturn(Single.just(5))
        subject.bchFeeOptions
            .test()
            .values()
            .first()
            .apply {
                priorityFee `should equal to` 5L
                regularFee `should equal to` 5L
            }
    }

    @Test
    fun `Use default BCH fee on API Error`() {
        whenever(walletOptionsDataManager.getBchFee())
            .thenReturn(Single.error(Throwable()))
        subject.bchFeeOptions
            .test()
            .values()
            .first()
            .apply {
                priorityFee `should equal to` 4
                regularFee `should equal to` 4
            }
    }

    @Test
    fun `Use default ETH fee on API Error`() {
        whenever(feeApi.ethFeeOptions)
            .thenReturn(Observable.error(Throwable()))
        whenever(environmentSettings.environment)
            .thenReturn(Environment.STAGING)
        subject.ethFeeOptions
            .test()
            .values()
            .first()
            .apply {
                priorityFee `should equal to` 23
                regularFee `should equal to` 23
                gasLimit `should equal to` 21000
                limits.min `should equal to` 23
                limits.max `should equal to` 23
            }
    }

    @Test
    fun `Use default BTC fee on API Error`() {
        whenever(feeApi.feeOptions)
            .thenReturn(Observable.error(Throwable()))
        whenever(environmentSettings.environment)
            .thenReturn(Environment.STAGING)
        subject.btcFeeOptions
            .test()
            .values()
            .first()
            .apply {
                priorityFee `should equal to` 11
                regularFee `should equal to` 5
                limits.min `should equal to` 2
                limits.max `should equal to` 16
            }
    }
}