package com.blockchain.transactions

import android.content.res.Resources
import info.blockchain.balance.CryptoCurrency
import piuk.blockchain.androidcoreui.R

internal class ResourceSendFundsResultLocalizer(private val resources: Resources) : SendFundsResultLocalizer {

    override fun localize(sendFundsResult: SendFundsResult): String =
        if (sendFundsResult.success) {
            resources.getString(R.string.transaction_submitted)
        } else
            localizeXlmSend(sendFundsResult) ?: resources.getString(R.string.transaction_failed)

    private fun localizeXlmSend(sendFundsResult: SendFundsResult): String? {
        if (sendFundsResult.sendDetails.from.cryptoCurrency != CryptoCurrency.XLM) return null
        return when (sendFundsResult.errorCode) {
            2 -> resources.getString(
                R.string.transaction_failed_min_send,
                sendFundsResult.errorValue?.toStringWithSymbol()
            )
            3 -> resources.getString(
                R.string.xlm_transaction_failed_min_balance_new_account,
                sendFundsResult.errorValue?.toStringWithSymbol()
            )
            4 -> sendFundsResult.errorValue.let {
                if (it == null) {
                    resources.getString(R.string.insufficient_funds)
                } else {
                    resources.getString(
                        R.string.insufficient_funds_with_max,
                        it.toStringWithSymbol()
                    )
                }
            }
            5 -> resources.getString(R.string.invalid_address)
            else -> null
        }
    }
}
