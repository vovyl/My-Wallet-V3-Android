package com.blockchain.sunriver

import com.blockchain.account.BalanceAndMin
import info.blockchain.balance.CryptoCurrency
import info.blockchain.balance.CryptoValue
import info.blockchain.balance.compareTo
import info.blockchain.balance.withMajorValue
import org.stellar.sdk.AssetTypeNative
import org.stellar.sdk.CreateAccountOperation
import org.stellar.sdk.KeyPair
import org.stellar.sdk.Memo
import org.stellar.sdk.Network
import org.stellar.sdk.Operation
import org.stellar.sdk.PaymentOperation
import org.stellar.sdk.Server
import org.stellar.sdk.Transaction
import org.stellar.sdk.requests.ErrorResponse
import org.stellar.sdk.requests.RequestBuilder
import org.stellar.sdk.requests.TooManyRequestsException
import org.stellar.sdk.responses.AccountResponse
import org.stellar.sdk.responses.TransactionResponse
import org.stellar.sdk.responses.operations.OperationResponse
import java.io.IOException
import java.math.BigDecimal
import java.math.BigInteger

internal class HorizonProxy(url: String) {

    private val server = Server(url)

    init {
        if (url.contains("test")) {
            Network.useTestNetwork()
        } else {
            Network.usePublicNetwork()
        }
    }

    fun accountExists(accountId: String) = accountExists(KeyPair.fromAccountId(accountId))

    private fun accountExists(keyPair: KeyPair) = findAccount(keyPair) != null

    fun getBalance(accountId: String) =
        findAccount(KeyPair.fromAccountId(accountId)).balance

    fun getBalanceAndMin(accountId: String) =
        findAccount(KeyPair.fromAccountId(accountId)).let { account ->
            BalanceAndMin(
                balance = account.balance,
                minimumBalance = account.minBalance(minReserve)
            )
        }

    private fun findAccount(keyPair: KeyPair): AccountResponse? {
        val accounts = server.accounts()
        return try {
            accounts.account(keyPair)
        } catch (e: ErrorResponse) {
            if (e.code == 404) {
                null
            } else {
                throw e
            }
        }
    }

    fun getTransactionList(accountId: String): List<OperationResponse> = try {
        server.operations()
            .order(RequestBuilder.Order.DESC)
            .limit(50)
            .forAccount(KeyPair.fromAccountId(accountId))
            .execute()
            .records
    } catch (e: ErrorResponse) {
        if (e.code == 404) {
            emptyList()
        } else {
            throw e
        }
    }

    @Throws(IOException::class, TooManyRequestsException::class)
    fun getTransaction(hash: String): TransactionResponse =
        server.transactions()
            .transaction(hash)

    fun sendTransaction(
        source: KeyPair,
        destinationAccountId: String,
        amount: CryptoValue
    ): SendResult =
        sendTransaction(source, destinationAccountId, amount, Memo.none())

    fun sendTransaction(
        source: KeyPair,
        destinationAccountId: String,
        amount: CryptoValue,
        memo: Memo
    ): SendResult {
        val result = dryRunTransaction(source, destinationAccountId, amount, memo)
        if (!result.success || result.transaction == null) {
            return result
        }
        result.transaction.sign(source)
        return SendResult(
            server.submitTransaction(result.transaction).isSuccess,
            result.transaction
        )
    }

    fun dryRunTransaction(
        source: KeyPair,
        destinationAccountId: String,
        amount: CryptoValue
    ): SendResult =
        dryRunTransaction(source, destinationAccountId, amount, Memo.none())

    fun dryRunTransaction(
        source: KeyPair,
        destinationAccountId: String,
        amount: CryptoValue,
        memo: Memo
    ): SendResult {
        if (amount.currency != CryptoCurrency.XLM) throw IllegalArgumentException()
        val destination: KeyPair = try {
            KeyPair.fromAccountId(destinationAccountId)
        } catch (e: Exception) {
            return SendResult(
                success = false,
                failureReason = FailureReason.BadDestinationAccountId
            )
        }
        if (amount < minSend) {
            return SendResult(
                success = false,
                failureReason = FailureReason.BelowMinimumSend,
                failureValue = minSend
            )
        }
        val destinationAccountExists = accountExists(destination)
        val newAccountMinBalance = minBalance(minReserve, subentryCount = 0)
        if (!destinationAccountExists && amount < newAccountMinBalance) {
            return SendResult(
                success = false,
                failureReason = FailureReason.BelowMinimumBalanceForNewAccount,
                failureValue = newAccountMinBalance
            )
        }
        val account = server.accounts().account(source)
        val transaction =
            createUnsignedTransaction(account, destination, destinationAccountExists, amount.toBigDecimal(), memo)
        val fee = CryptoValue.lumensFromStroop(transaction.fee.toBigInteger())
        val total = amount + fee
        val minBalance = minBalance(minReserve, account.subentryCount)
        if (account.balance < total + minBalance) {
            return SendResult(
                success = false,
                failureReason = FailureReason.InsufficientFunds,
                failureValue = account.balance - minBalance - fee
            )
        }
        return SendResult(
            true,
            transaction
        )
    }

    /**
     * TODO("AND-1601") Get min reserve dynamically.
     */
    private val minReserve = CryptoCurrency.XLM.withMajorValue(0.5.toBigDecimal())
    private val minSend = CryptoValue(CryptoCurrency.XLM, BigInteger.ONE)

    class SendResult(
        val success: Boolean,
        val transaction: Transaction? = null,
        val failureReason: FailureReason = FailureReason.Unknown,
        val failureValue: CryptoValue? = null
    )

    enum class FailureReason(val errorCode: Int) {

        Unknown(errorCode = 1),

        /**
         * The amount attempted to be sent was below that which we allow.
         */
        BelowMinimumSend(errorCode = 2),

        /**
         * The destination does exist and a send was attempted that did not fund it
         * with at least the minimum balance for an Horizon account.
         */
        BelowMinimumBalanceForNewAccount(errorCode = 3),

        /**
         * The amount attempted to be sent would not leave the source account with at
         * least the minimum balance required for an Horizon account.
         */
        InsufficientFunds(errorCode = 4),

        /**
         * The destination account id is not valid.
         */
        BadDestinationAccountId(errorCode = 5),
    }

    private fun createUnsignedTransaction(
        source: AccountResponse,
        destination: KeyPair,
        destinationAccountExists: Boolean,
        amount: BigDecimal,
        memo: Memo
    ): Transaction =
        Transaction.Builder(source)
            .addOperation(buildTransactionOperation(destination, destinationAccountExists, amount.toPlainString()))
            .addMemo(memo)
            .build()

    private fun buildTransactionOperation(
        destination: KeyPair,
        destinationAccountExists: Boolean,
        amount: String
    ): Operation =
        if (destinationAccountExists) {
            PaymentOperation.Builder(
                destination,
                AssetTypeNative(),
                amount
            ).build()
        } else {
            CreateAccountOperation.Builder(
                destination,
                amount
            ).build()
        }
}

private val AccountResponse?.balance: CryptoValue
    get() =
        this?.balances?.firstOrNull {
            it.assetType == "native" && it.assetCode == null
        }?.balance?.let { CryptoValue.lumensFromMajor(it.toBigDecimal()) }
            ?: CryptoValue.ZeroXlm

private fun AccountResponse?.minBalance(minReserve: CryptoValue): CryptoValue =
    this?.let { minBalance(minReserve, subentryCount) } ?: CryptoValue.ZeroXlm

private fun minBalance(minReserve: CryptoValue, subentryCount: Int) =
    CryptoValue.lumensFromMajor((2 + subentryCount).toBigDecimal() * minReserve.toBigDecimal())
