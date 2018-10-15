package com.blockchain.wallet

import info.blockchain.balance.CryptoCurrency

interface DefaultLabels {

    operator fun get(cryptoCurrency: CryptoCurrency): String
}
