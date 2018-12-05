package com.blockchain.lockbox.ui

import piuk.blockchain.androidcoreui.ui.base.View

interface LockboxLandingView : View {

    fun renderUiState(uiState: LockboxUiState)
}
