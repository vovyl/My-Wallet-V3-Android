package com.blockchain.lockbox.koin

import com.blockchain.features.FeatureNames
import com.blockchain.lockbox.data.LockboxDataManager
import com.blockchain.lockbox.ui.LockboxLandingPresenter
import org.koin.dsl.module.applicationContext

val lockboxModule = applicationContext {

    context("Payload") {

        factory { LockboxDataManager(get(), getProperty(FeatureNames.LOCKBOX)) }

        factory { LockboxLandingPresenter(get(), get()) }
    }
}