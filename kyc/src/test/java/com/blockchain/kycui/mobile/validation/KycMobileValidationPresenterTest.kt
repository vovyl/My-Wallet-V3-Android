package com.blockchain.kycui.mobile.validation

import com.blockchain.android.testutils.rxInit
import com.blockchain.kyc.datamanagers.nabu.NabuUserSync
import com.blockchain.kycui.mobile.entry.models.PhoneVerificationModel
import com.blockchain.kycui.mobile.validation.models.VerificationCode
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import org.amshove.kluent.`it returns`
import org.amshove.kluent.mock
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import piuk.blockchain.androidcore.data.settings.PhoneNumberUpdater

class KycMobileValidationPresenterTest {

    private lateinit var subject: KycMobileValidationPresenter
    private val view: KycMobileValidationView = mock()
    private val phoneNumberUpdater: PhoneNumberUpdater = mock()
    private val nabuUserSync: NabuUserSync = com.nhaarman.mockito_kotlin.mock {
        on { syncUser() } `it returns` Completable.complete()
    }

    @Suppress("unused")
    @get:Rule
    val initSchedulers = rxInit {
        mainTrampoline()
    }

    @Before
    fun setUp() {
        subject = KycMobileValidationPresenter(
            nabuUserSync,
            phoneNumberUpdater
        )
        subject.initView(view)
    }

    @Test
    fun `onViewReady, should progress page`() {
        // Arrange
        val phoneNumberSanitized = "+1234567890"
        val verificationCode = VerificationCode("VERIFICATION_CODE")
        val publishSubject = PublishSubject.create<Pair<PhoneVerificationModel, Unit>>()
        whenever(view.uiStateObservable).thenReturn(publishSubject)
        whenever(phoneNumberUpdater.verifySms(verificationCode.code))
            .thenReturn(Single.just(phoneNumberSanitized))
        // Act
        subject.onViewReady()
        publishSubject.onNext(
            PhoneVerificationModel(
                phoneNumberSanitized,
                verificationCode
            ) to Unit
        )
        // Assert
        verify(nabuUserSync).syncUser()
        verify(view).showProgressDialog()
        verify(view).dismissProgressDialog()
        verify(view).continueSignUp()
    }

    @Test
    fun `onViewReady, should throw exception and resubscribe for next event`() {
        // Arrange
        val phoneNumberSanitized = "+1234567890"
        val verificationCode = VerificationCode("VERIFICATION_CODE")
        val publishSubject = PublishSubject.create<Pair<PhoneVerificationModel, Unit>>()
        whenever(view.uiStateObservable).thenReturn(publishSubject)
        whenever(phoneNumberUpdater.verifySms(verificationCode.code))
            .thenReturn(Single.just(phoneNumberSanitized))
        whenever(nabuUserSync.syncUser())
            .thenReturn(Completable.error { Throwable() })
            .thenReturn(Completable.complete())
        val verificationModel = PhoneVerificationModel(phoneNumberSanitized, verificationCode)

        // Act
        subject.onViewReady()
        publishSubject.onNext(verificationModel to Unit)
        publishSubject.onNext(verificationModel to Unit)
        // Assert
        verify(view, times(2)).showProgressDialog()
        verify(view, times(2)).dismissProgressDialog()
        verify(nabuUserSync, times(2)).syncUser()
        verify(view).displayErrorDialog(any())
        verify(view).continueSignUp()
    }

    @Test
    fun `onViewReady, should throw exception and display error dialog`() {
        // Arrange
        val phoneNumberSanitized = "+1234567890"
        val jwt = "JWT"
        val verificationCode = VerificationCode("VERIFICATION_CODE")
        val publishSubject = PublishSubject.create<Pair<PhoneVerificationModel, Unit>>()
        whenever(view.uiStateObservable).thenReturn(publishSubject)
        whenever(phoneNumberUpdater.verifySms(verificationCode.code))
            .thenReturn(Single.just(phoneNumberSanitized))
        whenever(nabuUserSync.syncUser())
            .thenReturn(Completable.error { Throwable() })
        // Act
        subject.onViewReady()
        publishSubject.onNext(
            PhoneVerificationModel(
                phoneNumberSanitized,
                verificationCode
            ) to Unit
        )
        // Assert
        verify(view).showProgressDialog()
        verify(view).dismissProgressDialog()
        verify(view).displayErrorDialog(any())
        verify(nabuUserSync).syncUser()
    }
}
