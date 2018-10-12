package com.blockchain.lockbox.ui

import com.blockchain.android.testutils.rxInit
import com.blockchain.lockbox.data.LockboxDataManager
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Single
import org.amshove.kluent.`should equal to`
import org.amshove.kluent.mock
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import piuk.blockchain.androidcore.data.walletoptions.WalletOptionsDataManager

class LockboxLandingPresenterTest {

    private lateinit var subject: LockboxLandingPresenter
    private val lockboxDataManager: LockboxDataManager = mock()
    private val walletOptionsDataManager: WalletOptionsDataManager = mock()
    private val view: LockboxLandingView = mock()

    @get:Rule
    val rx = rxInit {
        mainTrampoline()
    }

    @Before
    fun setUp() {
        subject = LockboxLandingPresenter(lockboxDataManager, walletOptionsDataManager)
        subject.initView(view)
    }

    @Test
    fun `get wallet link`() {
        val walletLink = "https://wallet-link.com"
        whenever(walletOptionsDataManager.getWalletLink()).thenReturn(walletLink)
        subject.getWalletLink() `should equal to` walletLink
    }

    @Test
    fun `get com root link`() {
        val comRootLink = "https://com-root-link.com"
        whenever(walletOptionsDataManager.getComRootLink()).thenReturn(comRootLink)
        subject.getComRootLink() `should equal to` comRootLink
    }

    @Test
    fun `error loading lockbox status, should emit error`() {
        // Arrange
        whenever(lockboxDataManager.hasLockbox())
            .thenReturn(Single.error { Throwable() })
        // Act
        subject.onViewReady()
        // Assert
        verify(view).renderUiState(LockboxUiState.Loading)
        verify(view).renderUiState(LockboxUiState.Error)
        verifyNoMoreInteractions(view)
    }

    @Test
    fun `no lockbox`() {
        // Arrange
        whenever(lockboxDataManager.hasLockbox())
            .thenReturn(Single.just(false))
        // Act
        subject.onViewReady()
        // Assert
        verify(view).renderUiState(LockboxUiState.Loading)
        verify(view).renderUiState(LockboxUiState.NoLockbox)
        verifyNoMoreInteractions(view)
    }

    @Test
    fun `lockbox paired`() {
        // Arrange
        whenever(lockboxDataManager.hasLockbox())
            .thenReturn(Single.just(true))
        // Act
        subject.onViewReady()
        // Assert
        verify(view).renderUiState(LockboxUiState.Loading)
        verify(view).renderUiState(LockboxUiState.LockboxPaired)
        verifyNoMoreInteractions(view)
    }
}