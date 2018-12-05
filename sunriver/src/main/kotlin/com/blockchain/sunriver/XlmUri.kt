package com.blockchain.sunriver

import info.blockchain.balance.AccountReference

fun AccountReference.Xlm.toUri(): String = "web+stellar:pay?destination=" + this.accountId