package com.blockchain.koin

import info.blockchain.wallet.api.dust.BchDustService
import info.blockchain.wallet.api.dust.DustApi
import info.blockchain.wallet.api.dust.DustService
import info.blockchain.wallet.multiaddress.MultiAddressFactory
import info.blockchain.wallet.payload.BalanceManagerBch
import info.blockchain.wallet.payload.BalanceManagerBtc
import info.blockchain.wallet.payload.PayloadManager
import info.blockchain.wallet.payload.PayloadManagerWiper
import info.blockchain.wallet.prices.CurrentPriceApi
import info.blockchain.wallet.prices.PriceApi
import info.blockchain.wallet.prices.PriceEndpoints
import info.blockchain.wallet.prices.toCachedIndicativeFiatPriceService
import org.koin.KoinContext
import org.koin.dsl.module.applicationContext
import org.koin.standalone.StandAloneContext
import retrofit2.Retrofit

val walletModule = applicationContext {

    context("Payload") {

        bean { PayloadManager(get(), get(), get(), get()) }

        factory { MultiAddressFactory(get()) }

        factory { BalanceManagerBtc(get()) }

        factory { BalanceManagerBch(get()) }
    }

    factory { PriceApi(get(), get()) }

    bean { get<Retrofit>("api").create(PriceEndpoints::class.java) }

    factory { get<PriceApi>() as CurrentPriceApi }

    factory { get<CurrentPriceApi>().toCachedIndicativeFiatPriceService() }

    factory { BchDustService(get<Retrofit>("kotlin-api").create(DustApi::class.java), get()) as DustService }

    bean {
        object : PayloadManagerWiper {
            override fun wipe() {
                (StandAloneContext.koinContext as KoinContext).releaseContext("Payload")
            }
        } as PayloadManagerWiper
    }
}
