package piuk.blockchain.androidbuysell.models.coinify

/**
 * Currencies are ISO_4217 Strings, eg "USD", "BTC".
 *
 * @see [https://en.wikipedia.org/wiki/ISO_4217].
 */
data class QuoteRequest(
        val baseCurrency: String,
        val quoteCurrency: String,
        val baseAmount: Int = -1
)

/**
 * Currencies are ISO_4217 Strings, eg "USD", "BTC". Times are ISO_8601, eg "2016-04-01T12:27:36Z".
 *
 * @see [https://en.wikipedia.org/wiki/ISO_4217]
 * @see [https://en.wikipedia.org/wiki/ISO_8601]
 */
data class Quote(
        val baseCurrency: String,
        val quoteCurrency: String,
        val baseAmount: Int,
        val quoteAmount: Double,
        val issueTime: String,
        val expiryTime: String
)