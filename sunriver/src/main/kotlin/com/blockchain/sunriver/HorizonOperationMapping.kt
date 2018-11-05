package com.blockchain.sunriver

import com.blockchain.sunriver.models.XlmTransaction
import info.blockchain.balance.CryptoCurrency
import info.blockchain.balance.CryptoValue
import info.blockchain.balance.withMajorValue
import org.stellar.sdk.KeyPair
import org.stellar.sdk.responses.operations.CreateAccountOperationResponse
import org.stellar.sdk.responses.operations.OperationResponse
import org.stellar.sdk.responses.operations.PaymentOperationResponse

internal fun List<OperationResponse>.map(accountId: String): List<XlmTransaction> =
    filter { it is CreateAccountOperationResponse || it is PaymentOperationResponse }
        .map { mapOperationResponse(it, accountId) }

internal fun mapOperationResponse(
    operationResponse: OperationResponse,
    usersAccountId: String
): XlmTransaction =
    when (operationResponse) {
        is CreateAccountOperationResponse -> operationResponse.mapCreate(usersAccountId)
        is PaymentOperationResponse -> operationResponse.mapPayment(usersAccountId)
        else -> throw IllegalArgumentException("Unsupported operation type ${operationResponse.javaClass.simpleName}")
    }

private fun CreateAccountOperationResponse.mapCreate(usersAccountId: String) =
    XlmTransaction(
        timeStamp = createdAt,
        value = deltaValueForAccount(usersAccountId, funder, startingBalance),
        hash = transactionHash,
        to = account.toHorizonKeyPair().neuter(),
        from = funder.toHorizonKeyPair().neuter()
    )

private fun PaymentOperationResponse.mapPayment(usersAccountId: String) =
    XlmTransaction(
        timeStamp = createdAt,
        value = deltaValueForAccount(usersAccountId, from, amount),
        hash = transactionHash,
        to = to.toHorizonKeyPair().neuter(),
        from = from.toHorizonKeyPair().neuter()
    )

private fun deltaValueForAccount(
    usersAccountId: String,
    from: KeyPair,
    value: String
): CryptoValue {
    val valueAsBigDecimal = value.toBigDecimal()
    val deltaForThisAccount =
        if (from.accountId == usersAccountId) {
            valueAsBigDecimal.negate()
        } else {
            valueAsBigDecimal
        }
    return CryptoCurrency.XLM.withMajorValue(deltaForThisAccount)
}
