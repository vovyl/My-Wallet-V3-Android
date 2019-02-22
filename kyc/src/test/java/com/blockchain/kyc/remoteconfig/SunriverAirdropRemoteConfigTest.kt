package com.blockchain.kyc.remoteconfig

import com.blockchain.kyc.sunriverAirdropRemoteConfig
import com.blockchain.remoteconfig.RemoteConfiguration
import org.amshove.kluent.mock
import org.junit.Test

class SunriverAirdropRemoteConfigTest {

    private val remoteConfiguration: RemoteConfiguration = mock()

    @Test
    fun `should request remote config value - true`() {
        remoteConfiguration.givenEnabled("android_sunriver_airdrop_enabled")

        sunriverAirdropRemoteConfig(remoteConfiguration)
            .enabled
            .test()
            .assertValue(true)
    }

    @Test
    fun `should request remote config value - false`() {
        remoteConfiguration.givenDisabled("android_sunriver_airdrop_enabled")

        sunriverAirdropRemoteConfig(remoteConfiguration)
            .enabled
            .test()
            .assertValue(false)
    }
}
