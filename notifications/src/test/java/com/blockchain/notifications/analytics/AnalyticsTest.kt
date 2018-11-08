package com.blockchain.notifications.analytics

import com.google.firebase.analytics.FirebaseAnalytics
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.junit.Test

class AnalyticsTest {

    @Test
    fun `should log custom event`() {
        val mockFirebase = mock<FirebaseAnalytics>()
        val event = object : Loggable {
            override val eventName: String
                get() = "eventName"
        }

        Analytics(mockFirebase).logEvent(event)

        verify(mockFirebase).logEvent(event.eventName, null)
    }
}