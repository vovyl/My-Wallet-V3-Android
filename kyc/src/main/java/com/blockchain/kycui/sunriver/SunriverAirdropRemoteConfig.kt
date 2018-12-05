package com.blockchain.kycui.sunriver

import com.blockchain.remoteconfig.FeatureFlag
import com.blockchain.remoteconfig.RemoteConfiguration
import io.reactivex.Single

class SunriverAirdropRemoteConfig(
    private val remoteConfiguration: RemoteConfiguration
) : FeatureFlag {

    override val enabled: Single<Boolean>
        get() = remoteConfiguration.getIfFeatureEnabled(CONFIG_SUNRIVER_AIRDROP)

    internal companion object {

        const val CONFIG_SUNRIVER_AIRDROP = "android_sunriver_airdrop_enabled"
    }
}