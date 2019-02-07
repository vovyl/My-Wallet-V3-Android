package com.blockchain.kyc.remoteconfig

import com.blockchain.kyc.smsVerificationRemoteConfig
import com.blockchain.remoteconfig.RemoteConfiguration
import org.amshove.kluent.mock
import org.junit.Test

class SmsVerificationRemoteConfigTest {

    private val remoteConfiguration: RemoteConfiguration = mock()

    @Test
    fun `should request remote config value - true`() {
        remoteConfiguration.givenEnabled("android_sms_verification")

        smsVerificationRemoteConfig(remoteConfiguration)
            .enabled
            .test()
            .assertValue(true)
    }

    @Test
    fun `should request remote config value - false`() {
        remoteConfiguration.givenDisabled("android_sms_verification")

        smsVerificationRemoteConfig(remoteConfiguration)
            .enabled
            .test()
            .assertValue(false)
    }
}
