package piuk.blockchain.android.data.datamanagers.models

import com.blockchain.nabu.extensions.fromIso8601ToUtc
import com.blockchain.nabu.extensions.toLocalTime
import com.blockchain.sunriver.models.XlmTransaction
import info.blockchain.balance.CryptoCurrency
import info.blockchain.balance.CryptoValue
import info.blockchain.balance.compareTo
import info.blockchain.wallet.multiaddress.TransactionSummary
import piuk.blockchain.androidcore.data.transactions.models.Displayable
import java.math.BigInteger

class XlmDisplayable(
    private val xlmTransaction: XlmTransaction
) : Displayable() {
    override val cryptoCurrency: CryptoCurrency
        get() = CryptoCurrency.XLM
    override val direction: TransactionSummary.Direction
        get() = if (xlmTransaction.total > CryptoValue.ZeroXlm) {
            TransactionSummary.Direction.RECEIVED
        } else {
            TransactionSummary.Direction.SENT
        }
    override val timeStamp: Long
        get() = xlmTransaction.timeStamp.fromIso8601ToUtc()!!.toLocalTime().time.div(1000)
    override val total: BigInteger
        get() = xlmTransaction.total.amount.abs()
    override val fee: BigInteger
        get() = BigInteger.ZERO
    override val hash: String
        get() = xlmTransaction.hash
    override val inputsMap: HashMap<String, BigInteger>
        get() = hashMapOf(xlmTransaction.from.accountId to BigInteger.ZERO)
    override val outputsMap: HashMap<String, BigInteger>
        get() = hashMapOf(xlmTransaction.to.accountId to total)
    override val confirmations: Int
        get() = CryptoCurrency.XLM.requiredConfirmations
}