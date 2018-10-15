package com.blockchain.koin

import com.blockchain.sunriver.HorizonProxy
import com.blockchain.sunriver.XlmDataManager
import com.blockchain.sunriver.datamanager.XlmMetaDataInitializer
import org.koin.dsl.module.applicationContext

val sunriverModule = applicationContext {

    context("Payload") {

        factory { XlmDataManager(get(), get()) }

        factory { HorizonProxy(getProperty("HorizonURL")) }

        factory { XlmMetaDataInitializer(get(), get(), get(), get()) }
    }
}
