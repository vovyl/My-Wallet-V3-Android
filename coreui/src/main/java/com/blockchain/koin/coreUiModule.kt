package com.blockchain.koin

import com.blockchain.remoteconfig.RemoteConfig
import com.blockchain.remoteconfig.RemoteConfiguration
import com.blockchain.ui.chooser.AccountChooserPresenter
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import org.koin.dsl.module.applicationContext
import piuk.blockchain.androidcoreui.BuildConfig

val coreUiModule = applicationContext {

    context("Payload") {

        factory {
            AccountChooserPresenter(get(), get(), get())
        }
    }

    bean {
        val config = FirebaseRemoteConfigSettings.Builder()
            .setDeveloperModeEnabled(BuildConfig.DEBUG)
            .build()
        FirebaseRemoteConfig.getInstance().apply {
            setConfigSettings(config)
        }
    }

    factory { RemoteConfiguration(get()) }
        .bind(RemoteConfig::class)
}
