package com.blockchain.wallet

import android.content.res.Resources
import com.nhaarman.mockito_kotlin.mock
import info.blockchain.balance.CryptoCurrency
import org.amshove.kluent.`it returns`
import org.amshove.kluent.`should equal`
import org.junit.Test
import piuk.blockchain.androidcore.R

class ResourceDefaultLabelsTest {

    private val resources: Resources = mock {
        on { getString(R.string.default_wallet_name) } `it returns` "A - BTC"
        on { getString(R.string.eth_default_account_label) } `it returns` "B - ETH"
        on { getString(R.string.bch_default_account_label) } `it returns` "C - BCH"
        on { getString(R.string.xlm_default_account_label) } `it returns` "D - XLM"
    }

    private val defaultLabels: DefaultLabels =
        ResourceDefaultLabels(resources)

    @Test
    fun `btc default label`() {
        defaultLabels[CryptoCurrency.BTC] `should equal` "A - BTC"
    }

    @Test
    fun `ether default label`() {
        defaultLabels[CryptoCurrency.ETHER] `should equal` "B - ETH"
    }

    @Test
    fun `bch default label`() {
        defaultLabels[CryptoCurrency.BCH] `should equal` "C - BCH"
    }

    @Test
    fun `xlm default label`() {
        defaultLabels[CryptoCurrency.XLM] `should equal` "D - XLM"
    }
}
