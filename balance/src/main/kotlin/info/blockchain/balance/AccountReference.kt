package info.blockchain.balance

sealed class AccountReference(
    val cryptoCurrency: CryptoCurrency,
    val label: String
) {
    data class BitcoinLike(
        private val _cryptoCurrency: CryptoCurrency,
        private val _label: String,
        val xpub: String
    ) : AccountReference(_cryptoCurrency, _label)

    data class Ethereum(
        private val _label: String,
        val address: String
    ) : AccountReference(CryptoCurrency.ETHER, _label)

    data class Xlm(
        private val _label: String,
        val accountId: String
    ) : AccountReference(CryptoCurrency.XLM, _label)
}
