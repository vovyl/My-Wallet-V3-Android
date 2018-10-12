package com.blockchain.lockbox.data

import com.blockchain.android.testutils.rxInit
import com.blockchain.lockbox.data.models.LockboxMetadata
import com.blockchain.remoteconfig.FeatureFlag
import com.blockchain.testutils.getStringFromResource
import com.google.common.base.Optional
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Observable
import io.reactivex.Single
import org.amshove.kluent.mock
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import piuk.blockchain.androidcore.data.metadata.MetadataManager

class LockboxDataManagerTest {

    private lateinit var subject: LockboxDataManager
    private val metadataManager: MetadataManager = mock()
    private val remoteConfiguration: FeatureFlag = mock()

    @get:Rule
    val rx = rxInit {
        ioTrampoline()
    }

    @Before
    fun setUp() {
        subject = LockboxDataManager(metadataManager, remoteConfiguration)
    }

    @Test
    fun `should not be available`() {
        givenNotAvailable()
        subject.isLockboxAvailable()
            .test()
            .assertValue(false)
    }

    @Test
    fun `should be available`() {
        givenAvailable()
        subject.isLockboxAvailable()
            .test()
            .assertValue(true)
    }

    @Test
    fun `should return no lockbox on error`() {
        // Arrange
        givenAvailable()
        whenever(metadataManager.fetchMetadata(LockboxMetadata.MetaDataType))
            .thenReturn(Observable.error { Throwable() })
        // Act
        val testObserver = subject.hasLockbox().test()
        // Assert
        testObserver.assertComplete()
        testObserver.assertValue(false)
    }

    @Test
    fun `should return no lockbox as no data present`() {
        // Arrange
        givenAvailable()
        whenever(metadataManager.fetchMetadata(LockboxMetadata.MetaDataType))
            .thenReturn(Observable.just(Optional.absent()))
        // Act
        val testObserver = subject.hasLockbox().test()
        // Assert
        testObserver.assertComplete()
        testObserver.assertValue(false)
    }

    @Test
    fun `should return no lockbox as devices empty`() {
        // Arrange
        givenAvailable()
        whenever(metadataManager.fetchMetadata(LockboxMetadata.MetaDataType))
            .thenReturn(Observable.just(Optional.of("{\"devices\": []}")))
        // Act
        val testObserver = subject.hasLockbox().test()
        // Assert
        testObserver.assertComplete()
        testObserver.assertValue(false)
    }

    @Test
    fun `should return has lockbox`() {
        // Arrange
        givenAvailable()
        whenever(metadataManager.fetchMetadata(LockboxMetadata.MetaDataType))
            .thenReturn(
                Observable.just(
                    Optional.of(
                        getStringFromResource("lockbox/LockboxCompleteMetadata.json")
                    )
                )
            )
        // Act
        val testObserver = subject.hasLockbox().test()
        // Assert
        testObserver.assertComplete()
        testObserver.assertValue(true)
    }

    private fun givenAvailable() {
        whenever(remoteConfiguration.enabled).thenReturn(Single.just(true))
    }

    private fun givenNotAvailable() {
        whenever(remoteConfiguration.enabled).thenReturn(Single.just(false))
    }
}