package com.blockchain.sunriver

import info.blockchain.balance.CryptoCurrency
import info.blockchain.balance.CryptoValue
import info.blockchain.balance.compareTo
import info.blockchain.balance.withMajorValue
import org.stellar.sdk.AssetTypeNative
import org.stellar.sdk.CreateAccountOperation
import org.stellar.sdk.KeyPair
import org.stellar.sdk.Network
import org.stellar.sdk.Operation
import org.stellar.sdk.PaymentOperation
import org.stellar.sdk.Server
import org.stellar.sdk.Transaction
import org.stellar.sdk.requests.ErrorResponse
import org.stellar.sdk.responses.AccountResponse
import org.stellar.sdk.responses.operations.OperationResponse
import java.math.BigDecimal

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

    fun sendTransaction(source: KeyPair, destination: KeyPair, amount: CryptoValue): SendResult {
        val account = server.accounts().account(source)
        val transaction = createUnsignedTransaction(account, destination, amount.toBigDecimal())
        val fee = CryptoValue.lumensFromStroop(transaction.fee.toBigInteger())
        val total = amount + fee
        if (account.balance < total + minBalance) {
            return SendResult(false, transaction)
        }
        transaction.sign(source)
        return SendResult(
            server.submitTransaction(transaction).isSuccess,
            transaction
        )
    }

    /**
     * TODO("AND-1601") Get minimum balance dynamically.
     */
    private val minBalance = CryptoCurrency.XLM.withMajorValue(BigDecimal.ONE)

    class SendResult(
        val success: Boolean,
        val transaction: Transaction
    )

    private fun HorizonProxy.createUnsignedTransaction(
        source: AccountResponse,
        destination: KeyPair,
        amount: BigDecimal
    ): Transaction {
        return Transaction.Builder(source)
            .addOperation(buildTransactionOperation(destination, amount.toPlainString()))
            .build()
    }

    private fun HorizonProxy.buildTransactionOperation(destination: KeyPair, amount: String): Operation =
        if (accountExists(destination)) {
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
