package com.blockchain.notifications.links

import android.content.Intent
import android.net.Uri
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.dynamiclinks.PendingDynamicLinkData
import com.nhaarman.mockito_kotlin.argumentCaptor
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.amshove.kluent.`it returns`
import org.amshove.kluent.any
import org.junit.Test

class DynamicLinkHandlerTest {

    private val testOnSuccessListener = argumentCaptor<OnSuccessListener<PendingDynamicLinkData>>()
    private val testOnFailureListener = argumentCaptor<OnFailureListener>()

    @Test
    fun `returns uri if present`() {
        val uri: Uri = mock()
        val data = mock<PendingDynamicLinkData> {
            on { link } `it returns` uri
        }
        val task = mock<Task<PendingDynamicLinkData>> {
            on { addOnSuccessListener(testOnSuccessListener.capture()) } `it returns` it
            on { addOnFailureListener(testOnFailureListener.capture()) } `it returns` it
        }
        val dynamicLinks = mock<FirebaseDynamicLinks> {
            on { getDynamicLink(any<Intent>()) } `it returns` task
        }

        val testObserver = DynamicLinkHandler(dynamicLinks)
            .getPendingLinks(mock())
            .test()

        testOnSuccessListener.firstValue.onSuccess(data)

        testObserver
            .assertNoErrors()
            .assertComplete()
            .assertValue(uri)
    }

    @Test
    fun `completes if uri not present`() {
        val data = mock<PendingDynamicLinkData> {
            on { link } `it returns` null
        }
        val task = mock<Task<PendingDynamicLinkData>> {
            on { addOnSuccessListener(testOnSuccessListener.capture()) } `it returns` it
            on { addOnFailureListener(testOnFailureListener.capture()) } `it returns` it
        }
        val dynamicLinks = mock<FirebaseDynamicLinks> {
            on { getDynamicLink(any<Intent>()) } `it returns` task
        }

        val testObserver = DynamicLinkHandler(dynamicLinks)
            .getPendingLinks(mock())
            .test()

        testOnSuccessListener.firstValue.onSuccess(data)

        verify(dynamicLinks).getDynamicLink(any<Intent>())

        testObserver
            .assertNoErrors()
            .assertComplete()
            .assertNoValues()
    }

    @Test
    fun `returns failure present`() {
        val task = mock<Task<PendingDynamicLinkData>> {
            on { addOnSuccessListener(testOnSuccessListener.capture()) } `it returns` it
            on { addOnFailureListener(testOnFailureListener.capture()) } `it returns` it
        }
        val dynamicLinks = mock<FirebaseDynamicLinks> {
            on { getDynamicLink(any<Intent>()) } `it returns` task
        }

        val testObserver = DynamicLinkHandler(dynamicLinks)
            .getPendingLinks(mock())
            .test()

        testOnFailureListener.firstValue.onFailure(Exception())

        verify(dynamicLinks).getDynamicLink(any<Intent>())

        testObserver
            .assertError(Exception::class.java)
    }
}