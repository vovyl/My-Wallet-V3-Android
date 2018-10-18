package com.blockchain.sunriver

import com.blockchain.sunriver.datamanager.XlmMetaDataInitializer
import info.blockchain.balance.AccountReference
import info.blockchain.balance.CryptoValue
import io.reactivex.Completable
import io.reactivex.Single
import org.stellar.sdk.KeyPair
import org.stellar.sdk.responses.operations.CreateAccountOperationResponse
import org.stellar.sdk.responses.operations.OperationResponse
import org.stellar.sdk.responses.operations.PaymentOperationResponse

class XlmDataManager internal constructor(
    private val horizonProxy: HorizonProxy,
    private val metaDataInitializer: XlmMetaDataInitializer
) {

    fun getBalance(accountReference: AccountReference.Xlm): Single<CryptoValue> =
        Single.just(horizonProxy.getBalance(accountReference.accountId))

    fun getBalance(): Single<CryptoValue> =
        defaultAccount().flatMap { getBalance(it) }

    fun defaultAccount(): Single<AccountReference.Xlm> =
        defaultXlmAccount()
            .map { AccountReference.Xlm(it.label ?: "", it.publicKey) }

    fun getTransactionList(accountReference: AccountReference.Xlm): Single<List<XlmTransaction>> =
        Single.just(horizonProxy.getTransactionList(accountReference.accountId))
            .map { it.map() }

    fun getTransactionList(): Single<List<XlmTransaction>> =
        defaultAccount().flatMap { getTransactionList(it) }

    fun sendFromDefault(destination: HorizonKeyPair.Public, value: CryptoValue): Completable {
        return defaultXlmAccount()
            .map {
                horizonProxy.sendTransaction(
                    KeyPair.fromSecretSeed(it.secret),
                    destination.toKeyPair(),
                    value
                )
            }.map {
                if (it.success) {
                    1
                } else {
                    throw XlmSendException()
                }
            }
            .toCompletable()
    }

    private fun defaultXlmAccount() =
        metaDataInitializer.initWallet()
            .map { it.accounts!![it.defaultAccountIndex] }
}

class XlmSendException : RuntimeException()

internal fun List<OperationResponse>.map(): List<XlmTransaction> =
    this.filter { it is CreateAccountOperationResponse || it is PaymentOperationResponse }
        .map {
            when (it) {
                is CreateAccountOperationResponse -> it.mapCreate()
                is PaymentOperationResponse -> it.mapPayment()
                else -> throw IllegalArgumentException("Unsupported operation type ${this.javaClass.simpleName}")
            }
        }

private fun CreateAccountOperationResponse.mapCreate(): XlmTransaction = XlmTransaction(
    timeStamp = createdAt,
    total = CryptoValue.lumensFromMajor(startingBalance.toBigDecimal()),
    hash = transactionHash,
    to = account.toHorizonKeyPair().neuter(),
    from = funder.toHorizonKeyPair().neuter()
)

private fun PaymentOperationResponse.mapPayment(): XlmTransaction {
    val amount = amount.toBigDecimal().apply {
        if (type == "debit") negate()
    }
    return XlmTransaction(
        timeStamp = createdAt,
        total = CryptoValue.lumensFromMajor(amount),
        hash = transactionHash,
        to = to.toHorizonKeyPair().neuter(),
        from = from.toHorizonKeyPair().neuter()
    )
}

data class XlmTransaction(
    val timeStamp: String,
    val total: CryptoValue,
    val hash: String,
    val to: HorizonKeyPair.Public,
    val from: HorizonKeyPair.Public
)
