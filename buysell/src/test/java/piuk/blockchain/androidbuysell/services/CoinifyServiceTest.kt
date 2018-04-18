package piuk.blockchain.androidbuysell.services

import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import org.amshove.kluent.`should be instance of`
import org.amshove.kluent.`should equal to`
import org.amshove.kluent.`should equal`
import org.junit.Before
import org.junit.Test
import piuk.blockchain.androidbuysell.MockWebServerTest
import piuk.blockchain.androidbuysell.api.PATH_COINFY_AUTH
import piuk.blockchain.androidbuysell.api.PATH_COINFY_GET_TRADER
import piuk.blockchain.androidbuysell.api.PATH_COINFY_PREP_KYC
import piuk.blockchain.androidbuysell.api.PATH_COINFY_SIGNUP_TRADER
import piuk.blockchain.androidbuysell.api.PATH_COINFY_TRADES_PAYMENT_METHODS
import piuk.blockchain.androidbuysell.api.PATH_COINFY_TRADES_QUOTE
import piuk.blockchain.androidbuysell.models.coinify.AuthRequest
import piuk.blockchain.androidbuysell.models.coinify.CannotTradeReasonAdapter
import piuk.blockchain.androidbuysell.models.coinify.Completed
import piuk.blockchain.androidbuysell.models.coinify.ForcedDelay
import piuk.blockchain.androidbuysell.models.coinify.GrantType
import piuk.blockchain.androidbuysell.models.coinify.QuoteRequest
import piuk.blockchain.androidbuysell.models.coinify.ReviewStateAdapter
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
            .add(ReviewStateAdapter())
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
                        .setBody(TRADER_RESPONSE)
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
    fun `getTrader success`() {
        // Arrange
        server.enqueue(
                MockResponse()
                        .setResponseCode(200)
                        .setBody(TRADER_RESPONSE)
        )
        val accessToken = "ACCESS_TOKEN"
        // Act
        val testObserver = subject.getTrader(
                path = PATH_COINFY_GET_TRADER,
                accessToken = accessToken
        ).test()
        // Assert
        testObserver.awaitTerminalEvent()
        testObserver.assertComplete()
        testObserver.assertNoErrors()
        val traderResponse = testObserver.values().first()
        traderResponse.trader.id `should equal to` 754035
        traderResponse.trader.profile.address.countryCode `should equal to` "US"
        val request = server.takeRequest()
        request.path `should equal to` "/$PATH_COINFY_GET_TRADER"
        request.headers.get("Authorization") `should equal` "Bearer $accessToken"
    }

    @Test
    fun `auth success`() {
        // Arrange
        server.enqueue(
                MockResponse()
                        .setResponseCode(200)
                        .setBody(AUTH_RESPONSE)
        )
        // Act
        val testObserver = subject.auth(
                path = PATH_COINFY_AUTH,
                authRequest = AuthRequest(
                        grantType = GrantType.OfflineToken,
                        offlineToken = "OFFLINE_TOKEN"
                )
        ).test()
        // Assert
        testObserver.awaitTerminalEvent()
        testObserver.assertComplete()
        testObserver.assertNoErrors()
        val authResponse = testObserver.values().first()
        authResponse.tokenType `should equal` "bearer"
        authResponse.expiresIn `should equal` 1200
        val request = server.takeRequest()
        request.path `should equal to` "/$PATH_COINFY_AUTH"
        // Check Moshi's handling of enum class w/overridden toString method
        val inputAsString = request.body.inputStream().bufferedReader().use { it.readText() }
        val adapter = moshi.adapter(AuthRequest::class.java)
        val (grantType, offlineToken) = adapter.fromJson(inputAsString)!!
        grantType `should equal` GrantType.OfflineToken
        offlineToken `should equal to` "OFFLINE_TOKEN"
    }

    @Test
    fun `getKycReview success`() {
        // Arrange
        server.enqueue(
                MockResponse()
                        .setResponseCode(200)
                        .setBody(KYC_RESPONSE)
        )
        val accessToken = "ACCESS_TOKEN"
        val redirectUrl = "REDIRECT_URL"
        // Act
        val testObserver = subject.getKycReview(
                path = PATH_COINFY_PREP_KYC,
                redirectUrl = redirectUrl,
                accessToken = accessToken
        ).test()
        // Assert
        testObserver.awaitTerminalEvent()
        testObserver.assertComplete()
        testObserver.assertNoErrors()
        val kycResponse = testObserver.values().first()
        kycResponse.state `should equal` Completed
        val request = server.takeRequest()
        request.path `should equal to` "/$PATH_COINFY_PREP_KYC"
        request.headers.get("Authorization") `should equal` "Bearer $accessToken"
    }

    @Test
    fun `getQuote success`() {
        // Arrange
        server.enqueue(
                MockResponse()
                        .setResponseCode(200)
                        .setBody(QUOTE_RESPONSE)
        )
        val accessToken = "ACCESS_TOKEN"
        // Act
        val testObserver = subject.getQuote(
                path = PATH_COINFY_TRADES_QUOTE,
                quoteRequest = QuoteRequest("BTC", "USD"),
                accessToken = accessToken

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
        val request = server.takeRequest()
        request.path `should equal to` "/$PATH_COINFY_TRADES_QUOTE"
        request.headers.get("Authorization") `should equal` "Bearer $accessToken"
    }

    @Test
    fun `get payment methods success`() {
        // Arrange
        server.enqueue(
                MockResponse()
                        .setResponseCode(200)
                        .setBody(PAYMENT_METHODS_RESPONSE)
        )
        val accessToken = "ACCESS_TOKEN"
        // Act
        val testObserver = subject.getPaymentMethods(
                path = PATH_COINFY_TRADES_PAYMENT_METHODS,
                inCurrency = "USD",
                outCurrency = "BTC",
                accessToken = accessToken
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
        val request = server.takeRequest()
        request.path `should equal to` "/$PATH_COINFY_TRADES_PAYMENT_METHODS?inCurrency=USD&outCurrency=BTC"
        request.headers.get("Authorization") `should equal` "Bearer $accessToken"
    }

    companion object {

        private const val TRADER_RESPONSE = "{\n" +
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

        private const val AUTH_RESPONSE = "{\n" +
                "  \"access_token\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWV9.TJVA95OrM7E2cBab30RMHrHDcEfxjoYZgeFONFh7HgQ\",\n" +
                "  \"token_type\": \"bearer\",\n" +
                "  \"expires_in\": 1200,\n" +
                "  \"refresh_token\": \"wt5RoH8i6HkSQvI8kFpEBLEIB6lw8lOpYKHEz0ND9znDaAOtH1dFI32GqhvT9PGC\"\n" +
                "}"

        private const val KYC_RESPONSE = "{\n" +
                "  \"id\": 55555,\n" +
                "  \"state\": \"completed\",\n" +
                "  \"returnUrl\": \"https://mypage.com/kyc_complete\",\n" +
                "  \"redirectUrl\": \"https://example.com/url/to/perform/kyc/review\",\n" +
                "  \"externalId\": \"1234-abcd-5678-f33d\",\n" +
                "  \"updateTime\": \"2016-07-07T12:11:36Z\",\n" +
                "  \"createTime\": \"2016-07-07T12:10:19Z\"\n" +
                "}"

    }
}