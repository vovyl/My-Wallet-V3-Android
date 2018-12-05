package piuk.blockchain.androidcore.data.exchangerate

import com.nhaarman.mockito_kotlin.mock
import info.blockchain.wallet.prices.PriceApi
import io.reactivex.Single
import org.amshove.kluent.`it returns`
import org.amshove.kluent.`should equal`
import org.amshove.kluent.`should have key`
import org.junit.Test

class ExchangeRateServiceTest {

    @Test
    fun getExchangeRateMap() {
        val mockApi = mock<PriceApi> {
            on { getPriceIndexes("BTC") } `it returns` Single.just(mapOf("USD" to mock()))
        }
        mockApi.getPriceIndexes("BTC").test()
            .values()
            .first()
            .apply {
                this `should have key` "USD"
            }
    }

    @Test
    fun getHistoricPrice() {
        val mockApi = mock<PriceApi> {
            on { getHistoricPrice("ETH", "GBP", 100L) } `it returns` Single.just(500.0)
        }
        mockApi.getHistoricPrice("ETH", "GBP", 100).test()
            .values()
            .first()
            .apply {
                this `should equal` 500.0
            }
    }
}