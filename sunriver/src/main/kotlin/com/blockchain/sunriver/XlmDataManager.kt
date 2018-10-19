package com.blockchain.sunriver

import com.blockchain.sunriver.datamanager.XlmAccount
import com.blockchain.sunriver.datamanager.XlmMetaDataInitializer
import com.blockchain.transactions.TransactionSender
import info.blockchain.balance.AccountReference
import info.blockchain.balance.CryptoValue
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.stellar.sdk.KeyPair
import org.stellar.sdk.responses.operations.CreateAccountOperationResponse
import org.stellar.sdk.responses.operations.OperationResponse
import org.stellar.sdk.responses.operations.PaymentOperationResponse

class XlmDataManager internal constructor(
    private val horizonProxy: HorizonProxy,
    metaDataInitializer: XlmMetaDataInitializer
) : TransactionSender {

    override fun sendFunds(
        from: AccountReference,
        value: CryptoValue,
        toAddress: String
    ): Completable {
        return Single.fromCallable { HorizonKeyPair.createValidatedPublic(toAddress) }
            .flatMapCompletable {
                val source = from as? AccountReference.Xlm
                    ?: throw XlmSendException("Source account reference is not an Xlm reference")
                send(source, it, value)
            }
    }

    private val wallet = Single.defer { metaDataInitializer.initWallet() }

    fun getBalance(accountReference: AccountReference.Xlm): Single<CryptoValue> =
        Single.fromCallable { horizonProxy.getBalance(accountReference.accountId) }
            .subscribeOn(Schedulers.io())

    fun getBalance(): Single<CryptoValue> =
        defaultAccount().flatMap { getBalance(it) }

    fun defaultAccount(): Single<AccountReference.Xlm> =
        defaultXlmAccount()
            .map { AccountReference.Xlm(it.label ?: "", it.publicKey) }

    fun getTransactionList(accountReference: AccountReference.Xlm): Single<List<XlmTransaction>> =
        Single.fromCallable { horizonProxy.getTransactionList(accountReference.accountId).map() }
            .subscribeOn(Schedulers.io())

    fun getTransactionList(): Single<List<XlmTransaction>> =
        defaultAccount().flatMap { getTransactionList(it) }

    private fun send(
        source: AccountReference.Xlm,
        destination: HorizonKeyPair.Public,
        value: CryptoValue
    ): Completable =
        accountFor(source).send(destination, value)

    fun sendFromDefault(destination: HorizonKeyPair.Public, value: CryptoValue): Completable =
        defaultXlmAccount().send(destination, value)

    private fun defaultXlmAccount() =
        wallet.map { it.accounts!![it.defaultAccountIndex] }

    private fun accountFor(source: AccountReference.Xlm) =
        wallet.map {
            it.accounts?.firstOrNull { it.publicKey == source.accountId }
                ?: throw XlmSendException("Account not found in meta data")
        }

    private fun Single<XlmAccount>.send(destination: HorizonKeyPair.Public, value: CryptoValue) =
        map {
            horizonProxy.sendTransaction(
                KeyPair.fromSecretSeed(it.secret),
                destination.toKeyPair(),
                value
            )
        }.doOnSuccess {
            if (!it.success) {
                throw XlmSendException("Send failed")
            }
        }.toCompletable()
            .subscribeOn(Schedulers.io())
}

class XlmSendException(message: String) : RuntimeException(message)

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
