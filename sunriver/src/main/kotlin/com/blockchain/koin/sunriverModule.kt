package com.blockchain.koin

import com.blockchain.sunriver.HorizonProxy
import com.blockchain.sunriver.XlmDataManager
import org.koin.dsl.module.applicationContext

val sunriverModule = applicationContext {

    context("Payload") {

        factory { XlmDataManager(get()) }

        factory { HorizonProxy(getProperty("HorizonURL")) }
    }
}
