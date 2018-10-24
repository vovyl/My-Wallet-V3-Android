package piuk.blockchain.androidcore.data.payload

import com.blockchain.android.testutils.rxInit
import com.blockchain.ui.password.SecondPasswordHandler
import com.blockchain.ui.password.secondPassword
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.argumentCaptor
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyZeroInteractions
import io.reactivex.Maybe
import io.reactivex.subjects.PublishSubject
import org.amshove.kluent.mock
import org.junit.Rule
import org.junit.Test

class SecondPasswordRxExtensionTest {

    @get:Rule
    val initRx = rxInit {
        mainTrampoline()
    }

    @Test
    fun `no interactions before subscription`() {
        val secondPasswordHandler: SecondPasswordHandler = mock()
        secondPasswordHandler.secondPassword()
        verifyZeroInteractions(secondPasswordHandler)
    }

    @Test
    fun `no interactions until subscription`() {
        val secondPasswordHandler: SecondPasswordHandler = mock()
        secondPasswordHandler.secondPassword()
            .test()
            .assertNotComplete()
            .assertNoValues()
        verify(secondPasswordHandler).validateExtended(any())
    }

    @Test
    fun `on second password validated`() {
        val secondPasswordHandler: SecondPasswordHandler = mock()
        val test = secondPasswordHandler.secondPassword()
            .test()
        argumentCaptor<SecondPasswordHandler.ResultListenerEx>().apply {
            verify(secondPasswordHandler).validateExtended(capture())
            firstValue.onSecondPasswordValidated("Hello")
        }
        test.assertValue("Hello")
        test.assertComplete()
    }

    @Test
    fun `on no second password needed`() {
        val secondPasswordHandler: SecondPasswordHandler = mock()
        val test = secondPasswordHandler.secondPassword()
            .test()
        argumentCaptor<SecondPasswordHandler.ResultListenerEx>().apply {
            verify(secondPasswordHandler).validateExtended(capture())
            firstValue.onNoSecondPassword()
        }
        test.assertComplete()
            .assertNoValues()
    }

    @Test
    fun `on cancelled`() {
        val secondPasswordHandler: SecondPasswordHandler = mock()
        val test = secondPasswordHandler.secondPassword()
            .test()
        argumentCaptor<SecondPasswordHandler.ResultListenerEx>().apply {
            verify(secondPasswordHandler).validateExtended(capture())
            firstValue.onCancelled()
        }
        test.assertComplete()
            .assertNoValues()
    }

    @Test
    fun `no validation when used in concat with never`() {
        val secondPasswordHandler: SecondPasswordHandler = mock()
        Maybe.concat(
            Maybe.never(),
            secondPasswordHandler.secondPassword()
        ).firstElement()
            .test()
            .assertNotComplete()
            .assertNoValues()
        verify(secondPasswordHandler, never()).validateExtended(any())
    }

    @Test
    fun `validation when used in concat with empty`() {
        val secondPasswordHandler: SecondPasswordHandler = mock()
        Maybe.concat(
            Maybe.empty(),
            secondPasswordHandler.secondPassword()
        ).firstElement()
            .test()
            .assertNotComplete()
            .assertNoValues()
        verify(secondPasswordHandler).validateExtended(any())
    }

    @Test
    fun `validation when subject before completes`() {
        val secondPasswordHandler: SecondPasswordHandler = mock()
        val subject = PublishSubject.create<String>()
        Maybe.concat(
            subject.firstElement(),
            secondPasswordHandler.secondPassword()
        ).firstElement()
            .test()
            .assertNotComplete()
            .assertNoValues()
        verify(secondPasswordHandler, never()).validateExtended(any())
        subject.onComplete()
        verify(secondPasswordHandler).validateExtended(any())
    }

    @Test
    fun `no validation when subject before emits value`() {
        val secondPasswordHandler: SecondPasswordHandler = mock()
        val subject = PublishSubject.create<String>()
        val test = Maybe.concat(
            subject.firstElement(),
            secondPasswordHandler.secondPassword()
        ).firstElement()
            .test()
        test.assertNotComplete()
            .assertNoValues()
        verify(secondPasswordHandler, never()).validateExtended(any())
        subject.onNext("No Password")
        test.assertValue("No Password")
            .assertComplete()
        verify(secondPasswordHandler, never()).validateExtended(any())
    }
}
