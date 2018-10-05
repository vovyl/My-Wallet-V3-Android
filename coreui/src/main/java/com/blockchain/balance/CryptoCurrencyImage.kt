package com.blockchain.balance

import android.support.annotation.DrawableRes
import info.blockchain.balance.CryptoCurrency
import piuk.blockchain.androidcoreui.R

@DrawableRes
fun CryptoCurrency.layerListDrawableRes() =
    when (this) {
        CryptoCurrency.BTC -> R.drawable.layer_list_bitcoin
        CryptoCurrency.ETHER -> R.drawable.layer_list_eth
        CryptoCurrency.BCH -> R.drawable.layer_list_bitcoin_cash
        CryptoCurrency.XLM -> R.drawable.layer_list_lumen
    }

@DrawableRes
fun CryptoCurrency.drawableRes() =
    when (this) {
        CryptoCurrency.BTC -> R.drawable.vector_bitcoin
        CryptoCurrency.ETHER -> R.drawable.vector_eth
        CryptoCurrency.BCH -> R.drawable.vector_bitcoin_cash
        CryptoCurrency.XLM -> R.drawable.vector_stellar_rocket
    }

fun canTint(@DrawableRes drawableRes: Int) =
    when (drawableRes) {
        R.drawable.vector_stellar_rocket,
        R.drawable.layer_list_lumen -> false
        else -> true
    }
