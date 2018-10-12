package com.blockchain.lockbox.data.remoteconfig

import com.blockchain.lockbox.data.remoteconfig.LockboxRemoteConfig.Companion.CONFIG_LOCKBOX
import com.blockchain.remoteconfig.RemoteConfiguration
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Single
import org.amshove.kluent.mock
import org.junit.Test

class LockboxRemoteConfigTest {

    private val remoteConfiguration: RemoteConfiguration = mock()

    @Test
    fun `should request remote config value`() {
        whenever(remoteConfiguration.getIfFeatureEnabled(CONFIG_LOCKBOX))
            .thenReturn(Single.just(true))

        LockboxRemoteConfig(remoteConfiguration).enabled
            .test()
            .assertValue(true)

        verify(remoteConfiguration).getIfFeatureEnabled(CONFIG_LOCKBOX)
    }
}