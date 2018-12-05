package com.blockchain.remoteconfig

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import io.reactivex.Single
import timber.log.Timber

interface RemoteConfig {

    fun getIfFeatureEnabled(key: String): Single<Boolean>
}

class RemoteConfiguration(private val remoteConfig: FirebaseRemoteConfig) : RemoteConfig {

    private val configuration: Single<FirebaseRemoteConfig> = Single.just(remoteConfig.fetch())
        .cache()
        .doOnSuccess { remoteConfig.activateFetched() }
        .doOnError { Timber.e(it, "Failed to load Firebase Remote Config") }
        .map { remoteConfig }

    override fun getIfFeatureEnabled(key: String): Single<Boolean> =
        configuration.map { it.getBoolean(key) }
}