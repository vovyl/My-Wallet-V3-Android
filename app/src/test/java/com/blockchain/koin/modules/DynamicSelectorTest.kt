package com.blockchain.koin.modules

import com.blockchain.kycui.settings.KycStatusHelper
import com.blockchain.kycui.settings.SettingsKycState
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Single
import org.amshove.kluent.mock
import org.junit.Test

class DynamicSelectorTest {

    private val kycStatusHelper: KycStatusHelper = mock()

    @Test
    fun `on hidden, should throw`() {
        whenever(kycStatusHelper.getSettingsKycState())
            .thenReturn(Single.just(SettingsKycState.Hidden))

        dynamicSelector(kycStatusHelper, mock()).getMorphMethod()
            .test()
            .assertError(IllegalStateException::class.java)
    }

    @Test
    fun `on verified, should return Homebrew`() {
        whenever(kycStatusHelper.getSettingsKycState())
            .thenReturn(Single.just(SettingsKycState.Verified))

        dynamicSelector(kycStatusHelper, mock()).getMorphMethod()
            .test()
            .assertValue(MorphMethodType.HomeBrew)
    }

    @Test
    fun `unverified, should return Kyc`() {
        whenever(kycStatusHelper.getSettingsKycState())
            .thenReturn(Single.just(SettingsKycState.Unverified))

        dynamicSelector(kycStatusHelper, mock()).getMorphMethod()
            .test()
            .assertValue(MorphMethodType.Kyc)
    }
}