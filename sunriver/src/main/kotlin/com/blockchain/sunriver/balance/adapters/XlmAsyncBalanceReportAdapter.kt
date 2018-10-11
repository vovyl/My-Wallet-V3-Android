package com.blockchain.sunriver.balance.adapters

import com.blockchain.balance.AsyncBalanceReporter
import com.blockchain.sunriver.XlmDataManager
import info.blockchain.balance.AccountReference
import info.blockchain.balance.CryptoValue
import io.reactivex.Single

fun XlmDataManager.toAsyncBalanceReporter(): AsyncBalanceReporter =
    XlmAsyncBalanceReportAdapter(this)

private class XlmAsyncBalanceReportAdapter(
    private val xlmDataManager: XlmDataManager
) : AsyncBalanceReporter {

    override fun entireBalance() = xlmDataManager.getBalance()

    private val zero = Single.just(CryptoValue.ZeroXlm)

    override fun watchOnlyBalance(): Single<CryptoValue> = zero

    override fun importedAddressBalance(): Single<CryptoValue> = zero

    override fun addressBalance(address: String): Single<CryptoValue> =
        xlmDataManager.getBalance(
            AccountReference.Xlm(
                _label = "",
                accountId = address
            )
        )
}
