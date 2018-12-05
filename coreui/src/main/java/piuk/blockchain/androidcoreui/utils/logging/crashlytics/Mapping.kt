package piuk.blockchain.androidcoreui.utils.logging.crashlytics

import com.crashlytics.android.answers.CustomEvent
import piuk.blockchain.androidcoreui.utils.logging.CustomEventBuilder

fun CustomEventBuilder.buildCrashlyticsEvent() =
    CustomEvent(eventName)
        .apply {
            build { key, value ->
                putCustomAttribute(key, value)
            }
        }
