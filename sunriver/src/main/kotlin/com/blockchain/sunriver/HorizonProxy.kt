package com.blockchain.sunriver

import info.blockchain.balance.CryptoValue
import org.stellar.sdk.KeyPair
import org.stellar.sdk.Server
import org.stellar.sdk.requests.ErrorResponse
import org.stellar.sdk.responses.AccountResponse

internal class HorizonProxy(url: String) {

    private val server = Server(url)

    fun getBalance(accountId: String): CryptoValue {
        val accounts = server.accounts()
        val account: AccountResponse
        try {
            account = accounts.account(KeyPair.fromAccountId(accountId))
        } catch (e: ErrorResponse) {
            if (e.code == 404) {
                return CryptoValue.ZeroXlm
            } else {
                throw e
            }
        }
        return account.balances.firstOrNull {
            it.assetType == "native" && it.assetCode == null
        }?.balance?.let { CryptoValue.lumensFromMajor(it.toBigDecimal()) }
            ?: CryptoValue.ZeroXlm
    }
}
