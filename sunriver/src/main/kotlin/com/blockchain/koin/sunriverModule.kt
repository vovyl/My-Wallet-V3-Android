package com.blockchain.koin

import com.blockchain.sunriver.HorizonProxy
import com.blockchain.sunriver.XlmDataManager
import com.blockchain.sunriver.XlmSecretAccess
import com.blockchain.sunriver.datamanager.XlmMetaDataInitializer
import com.blockchain.transactions.TransactionSender
import com.blockchain.account.DefaultAccountDataManager
import org.koin.dsl.module.applicationContext

val sunriverModule = applicationContext {

    context("Payload") {

        factory { XlmSecretAccess(get()) }

        factory { XlmDataManager(get(), get(), get()) }
            .bind(TransactionSender::class)
            .bind(DefaultAccountDataManager::class)

        factory { HorizonProxy(getProperty("HorizonURL")) }

        bean { XlmMetaDataInitializer(get(), get(), get(), get()) }
    }
}
