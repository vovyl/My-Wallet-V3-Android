package piuk.blockchain.androidcoreui.utils.logging

import com.blockchain.logging.CustomEventBuilder
import com.blockchain.logging.EventLogger
import com.crashlytics.android.answers.Answers
import piuk.blockchain.androidcoreui.utils.logging.crashlytics.buildCrashlyticsEvent

internal class AnswersEventLogger(private val answers: Answers) : EventLogger {

    override fun logEvent(customEventBuilder: CustomEventBuilder) {
        answers.logCustom(customEventBuilder.buildCrashlyticsEvent())
    }
}