package com.blockchain.wallet

import android.content.res.Resources
import info.blockchain.balance.CryptoCurrency
import piuk.blockchain.androidcore.R

internal class ResourceDefaultLabels(
    private val resources: Resources
) : DefaultLabels {

    override fun get(cryptoCurrency: CryptoCurrency): String =
        when (cryptoCurrency) {
            CryptoCurrency.BTC -> resources.getString(R.string.default_wallet_name)
            CryptoCurrency.ETHER -> resources.getString(R.string.eth_default_account_label)
            CryptoCurrency.BCH -> resources.getString(R.string.bch_default_account_label)
            CryptoCurrency.XLM -> resources.getString(R.string.xlm_default_account_label)
        }
}
