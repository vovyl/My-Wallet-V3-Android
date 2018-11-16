package piuk.blockchain.androidcoreui.utils.logging

import com.blockchain.logging.CustomEventBuilder
import com.crashlytics.android.answers.Answers
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.junit.Test

class AnswersEventLoggerTest {

    @Test
    fun `should log custom event`() {
        val answers: Answers = mock()

        AnswersEventLogger(answers).logEvent(TestCustomEvent().putTestAttributes("key", "value"))

        verify(answers).logCustom(any())
    }

    private class TestCustomEvent : CustomEventBuilder("test") {

        fun putTestAttributes(key: String, value: String): TestCustomEvent {
            putCustomAttribute(key, value)
            return this
        }
    }
}