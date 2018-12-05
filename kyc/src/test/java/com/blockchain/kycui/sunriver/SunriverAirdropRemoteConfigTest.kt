package com.blockchain.kycui.sunriver

import com.blockchain.remoteconfig.RemoteConfiguration
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Single
import org.amshove.kluent.mock
import org.junit.Test

class SunriverAirdropRemoteConfigTest {

    private val remoteConfiguration: RemoteConfiguration = mock()

    @Test
    fun `should request remote config value`() {
        whenever(remoteConfiguration.getIfFeatureEnabled(SunriverAirdropRemoteConfig.CONFIG_SUNRIVER_AIRDROP))
            .thenReturn(Single.just(true))

        SunriverAirdropRemoteConfig(remoteConfiguration).enabled
            .test()
            .assertValue(true)

        verify(remoteConfiguration).getIfFeatureEnabled(SunriverAirdropRemoteConfig.CONFIG_SUNRIVER_AIRDROP)
    }
}