package com.blockchain.lockbox.data.remoteconfig

import com.blockchain.remoteconfig.FeatureFlag
import com.blockchain.remoteconfig.RemoteConfiguration
import io.reactivex.Single

class LockboxRemoteConfig(
    private val remoteConfiguration: RemoteConfiguration
) : FeatureFlag {

    override val enabled: Single<Boolean>
        get() = remoteConfiguration.getIfFeatureEnabled(CONFIG_LOCKBOX)

    internal companion object {

        const val CONFIG_LOCKBOX = "android_lockbox_enabled"
    }
}