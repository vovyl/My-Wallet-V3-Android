package piuk.blockchain.androidbuysell.services

import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import org.amshove.kluent.`should be instance of`
import org.amshove.kluent.`should equal to`
import org.junit.Before
import org.junit.Test
import piuk.blockchain.androidbuysell.MockWebServerTest
import piuk.blockchain.androidbuysell.api.PATH_COINFY_SIGNUP_TRADER
import piuk.blockchain.androidbuysell.api.PATH_COINFY_TRADES_PAYMENT_METHODS
import piuk.blockchain.androidbuysell.api.PATH_COINFY_TRADES_QUOTE
import piuk.blockchain.androidbuysell.models.coinify.CannotTradeReasonAdapter
import piuk.blockchain.androidbuysell.models.coinify.ForcedDelay
import piuk.blockchain.androidbuysell.models.coinify.QuoteRequest
import piuk.blockchain.androidbuysell.models.coinify.SignUpDetails
import piuk.blockchain.androidcore.data.rxjava.RxBus
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory

class CoinifyServiceTest : MockWebServerTest() {

    private lateinit var subject: CoinifyService
    private val rxBus = RxBus()
    private val moshi: Moshi = Moshi.Builder()
            .add(CannotTradeReasonAdapter())
            .build()
    private val moshiConverterFactory = MoshiConverterFactory.create(moshi)
    private val rxJava2CallAdapterFactory = RxJava2CallAdapterFactory.create()

    @Before
    override fun setUp() {
        super.setUp()

        val okHttpClient = OkHttpClient.Builder()
                .build()
        val retrofit = Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl(server.url("/").toString())
                .addConverterFactory(moshiConverterFactory)
                .addCallAdapterFactory(rxJava2CallAdapterFactory)
                .build()

        subject = CoinifyService(retrofit, rxBus)
    }

    @Test
    fun `signUp success`() {
        // Arrange
        server.enqueue(
                MockResponse()
                        .setResponseCode(200)
                        .setBody(SIGN_UP_RESPONSE)
        )
        // Act
        val testObserver = subject.signUp(
                path = PATH_COINFY_SIGNUP_TRADER,
                signUpDetails = SignUpDetails.basicSignUp(
                        "example@email.com",
                        "USD",
                        "US",
                        "token"
                )
        ).test()
        // Assert
        testObserver.awaitTerminalEvent()
        testObserver.assertComplete()
        testObserver.assertNoErrors()
        val traderResponse = testObserver.values().first()
        traderResponse.trader.id `should equal to` 754035
        traderResponse.trader.profile.address.countryCode `should equal to` "US"
        server.takeRequest().path `should equal to` "/$PATH_COINFY_SIGNUP_TRADER"
    }

    @Test
    fun `getQuote success`() {
        // Arrange
        server.enqueue(
                MockResponse()
                        .setResponseCode(200)
                        .setBody(QUOTE_RESPONSE)
        )
        // Act
        val testObserver = subject.getQuote(
                path = PATH_COINFY_TRADES_QUOTE,
                quoteRequest = QuoteRequest("BTC", "USD")
        ).test()
        // Assert
        testObserver.awaitTerminalEvent()
        testObserver.assertComplete()
        testObserver.assertNoErrors()
        val quote = testObserver.values().first()
        quote.baseCurrency `should equal to` "BTC"
        quote.quoteCurrency `should equal to` "USD"
        quote.baseAmount `should equal to` -1
        quote.quoteAmount `should equal to` 8329.89
        server.takeRequest().path `should equal to` "/$PATH_COINFY_TRADES_QUOTE"
    }

    @Test
    fun `get payment methods success`() {
        // Arrange
        server.enqueue(
                MockResponse()
                        .setResponseCode(200)
                        .setBody(PAYMENT_METHODS_RESPONSE)
        )
        // Act
        val testObserver = subject.getPaymentMethods(
                path = PATH_COINFY_TRADES_PAYMENT_METHODS,
                inCurrency = "USD",
                outCurrency = "BTC"
        ).test()
        // Assert
        testObserver.awaitTerminalEvent()
        testObserver.assertComplete()
        testObserver.assertNoErrors()
        val methods = testObserver.values().first()
        val bankInMethod = methods.first()
        bankInMethod.inMedium `should equal to` "bank"
        val blockchainInMethod = methods[1]
        blockchainInMethod.inMedium `should equal to` "blockchain"
        val cardInMethod = methods[2]
        cardInMethod.inMedium `should equal to` "card"
        cardInMethod.canTrade `should equal to` false
        cardInMethod.cannotTradeReasons!!.first() `should be instance of` ForcedDelay::class
        server.takeRequest().path `should equal to` "/$PATH_COINFY_TRADES_PAYMENT_METHODS?inCurrency=USD&outCurrency=BTC"
    }

    companion object {

        private const val SIGN_UP_RESPONSE = "{\n" +
                "  \"trader\": {\n" +
                "    \"id\": 754035,\n" +
                "    \"email\": \"example@email.com\",\n" +
                "    \"defaultCurrency\": \"USD\",\n" +
                "    \"profile\": {\n" +
                "      \"address\": {\n" +
                "        \"country\": \"US\"\n" +
                "      }\n" +
                "    },\n" +
                "    \"level\": {}\n" +
                "  },\n" +
                "  \"offlineToken\": \"aGFja2VydHlwZXIuY29tIGlzIG15IElERQ==\"\n" +
                "}"

        private const val QUOTE_RESPONSE = "{\n" +
                "  \"baseCurrency\": \"BTC\",\n" +
                "  \"quoteCurrency\": \"USD\",\n" +
                "  \"baseAmount\": -1,\n" +
                "  \"quoteAmount\": 8329.89,\n" +
                "  \"issueTime\": \"2018-04-13T12:42:32.000Z\",\n" +
                "  \"expiryTime\": \"2018-04-13T12:42:32.000Z\"\n" +
                "}"

        private const val PAYMENT_METHODS_RESPONSE = "" +
                "[\n" +
                "    {\n" +
                "      \"inMedium\": \"bank\",           \n" +
                "      \"outMedium\": \"blockchain\",    \n" +
                "      \"name\": \"Buy bitcoins with bank transfer\",    \n" +
                "      \"inCurrencies\": [\"DKK\", \"EUR\", \"GBP\", \"USD\"],\n" +
                "      \"outCurrencies\": [\"BTC\"],\n" +
                "      \"inFixedFees\": {\n" +
                "        \"DKK\": 0,\n" +
                "        \"EUR\": 0,\n" +
                "        \"GBP\": 0,\n" +
                "        \"USD\": 0\n" +
                "      },\n" +
                "      \"inPercentageFee\": 0,\n" +
                "      \"outFixedFees\": {\n" +
                "        \"BTC\": 0.001\n" +
                "      },\n" +
                "      \"outPercentageFee\": 0,\n" +
                "      \"minimumInAmounts\": {\n" +
                "        \"DKK\": 75.86,\n" +
                "        \"EUR\": 10.00,\n" +
                "        \"GBP\": 8.00,\n" +
                "        \"USD\": 12.50\n" +
                "      },\n" +
                "      \"limitInAmounts\": {\n" +
                "        \"DKK\": 7500.86,\n" +
                "        \"EUR\": 1000.00,\n" +
                "        \"GBP\": 8000.00,\n" +
                "        \"USD\": 1200.50\n" +
                "      },\n" +
                "      \"limitOutAmounts\": {\n" +
                "        \"BTC\": 0.8\n" +
                "      },\n" +
                "      \"canTrade\": true\n" +
                "    },\n" +
                "    {\n" +
                "      \"inMedium\": \"blockchain\",           \n" +
                "      \"outMedium\": \"bank\",    \n" +
                "      \"name\": \"Sell bitcoints to bank transfer\",    \n" +
                "      \"inCurrencies\": [\"BTC\"],\n" +
                "      \"outCurrencies\": [\"DKK\", \"EUR\", \"GBP\", \"USD\"],\n" +
                "      \"inFixedFees\": {\n" +
                "        \"BTC\": 0\n" +
                "      },\n" +
                "      \"inPercentageFee\": 0,\n" +
                "      \"outFixedFees\": {\n" +
                "        \"DKK\": 40.00,\n" +
                "        \"EUR\": 5.40,\n" +
                "        \"GBP\": 3.70,\n" +
                "        \"USD\": 6.10\n" +
                "      },\n" +
                "      \"outPercentageFee\": 0,\n" +
                "      \"minimumInAmounts\": {\n" +
                "        \"BTC\": 0.0102\n" +
                "      },\n" +
                "      \"limitInAmounts\": {\n" +
                "        \"BTC\": 1.86\n" +
                "      },\n" +
                "      \"limitOutAmounts\": {\n" +
                "        \"DKK\": 7500.86,\n" +
                "        \"EUR\": 1000.00,\n" +
                "        \"GBP\": 8000.00,\n" +
                "        \"USD\": 1200.50\n" +
                "      },\n" +
                "      \"canTrade\": true\n" +
                "    },\n" +
                "    {\n" +
                "      \"inMedium\": \"card\",           \n" +
                "      \"outMedium\": \"blockchain\",    \n" +
                "      \"name\": \"Buy bitcoins with card transfer\",    \n" +
                "      \"inCurrencies\": [\"DKK\", \"EUR\", \"GBP\", \"USD\"],\n" +
                "      \"outCurrencies\": [\"BTC\"],\n" +
                "      \"inFixedFees\": {\n" +
                "        \"DKK\": 0,\n" +
                "        \"EUR\": 0,\n" +
                "        \"GBP\": 0,\n" +
                "        \"USD\": 0\n" +
                "      },\n" +
                "      \"inPercentageFee\": 3,\n" +
                "      \"outFixedFees\": {\n" +
                "        \"BTC\": 0.001\n" +
                "      },\n" +
                "      \"outPercentageFee\": 0,\n" +
                "      \"minimumInAmounts\": {\n" +
                "        \"DKK\": 75.86,\n" +
                "        \"EUR\": 10.00,\n" +
                "        \"GBP\": 8.00,\n" +
                "        \"USD\": 12.50\n" +
                "      },\n" +
                "      \"limitInAmounts\": {\n" +
                "        \"DKK\": 75.86,\n" +
                "        \"EUR\": 10.00,\n" +
                "        \"GBP\": 8.00,\n" +
                "        \"USD\": 12.50\n" +
                "      },\n" +
                "      \"limitOutAmounts\": {\n" +
                "        \"BTC\": 0.8\n" +
                "      },\n" +
                "      \"canTrade\": false,\n" +
                "      \"cannotTradeReasons\": [\n" +
                "        {\n" +
                "          \"reasonCode\": \"forced_delay\",\n" +
                "          \"delayEnd\": \"2016-04-01T12:27:36Z\"\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "]"

    }
}