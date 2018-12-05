package piuk.blockchain.androidcore.data.fees

import com.nhaarman.mockito_kotlin.whenever
import info.blockchain.wallet.api.FeeApi
import io.reactivex.Single
import org.amshove.kluent.`should equal to`
import org.amshove.kluent.mock
import org.junit.Before
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
}