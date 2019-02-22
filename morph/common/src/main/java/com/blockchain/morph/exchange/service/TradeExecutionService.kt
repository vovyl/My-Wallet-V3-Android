package com.blockchain.morph.exchange.service

import com.blockchain.morph.CoinPair
import com.blockchain.morph.exchange.mvi.Quote
import info.blockchain.balance.CryptoValue
import info.blockchain.balance.FiatValue
import io.reactivex.Completable
import io.reactivex.Single

interface TradeExecutionService {

    fun executeTrade(
        quote: Quote,
        destinationAddress: String,
        refundAddress: String
    ): Single<TradeTransaction>

    fun putTradeFailureReason(
        tradeRequest: TradeTransaction,
        txHash: String?,
        message: String?
    ): Completable
}

interface TradeTransaction {
    val id: String
    val createdAt: String
    val pair: CoinPair
    val fee: CryptoValue
    val fiatValue: FiatValue
    val refundAddress: String
    val depositAddress: String
    val depositTextMemo: String?
    val deposit: CryptoValue
    val withdrawalAddress: String
    val withdrawal: CryptoValue
    val hashOut: String?
}
