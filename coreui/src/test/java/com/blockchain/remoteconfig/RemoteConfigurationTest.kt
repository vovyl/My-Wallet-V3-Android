package com.blockchain.remoteconfig

import com.google.android.gms.tasks.Task
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import org.amshove.kluent.mock
import org.junit.Test

class RemoteConfigurationTest {

    private val firebaseRemoteConfig: FirebaseRemoteConfig = mock()

    @Test
    fun `should ask firebase for boolean config`() {
        // Arrange
        val someKey = "some_key"
        val task: Task<Void> = mock()
        whenever(firebaseRemoteConfig.fetch()).thenReturn(task)
        whenever(firebaseRemoteConfig.activateFetched()).thenReturn(true)
        whenever(firebaseRemoteConfig.getBoolean(someKey)).thenReturn(true)
        // Act
        val testObserver = RemoteConfiguration(firebaseRemoteConfig).getIfFeatureEnabled(someKey).test()
        // Assert
        testObserver.assertValue(true)
        verify(firebaseRemoteConfig).fetch()
        verify(firebaseRemoteConfig).activateFetched()
        verify(firebaseRemoteConfig).getBoolean(someKey)
    }
}