package piuk.blockchain.androidbuysell.utils

import android.annotation.SuppressLint
import timber.log.Timber
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Converts a [String] from an ISO 8601 date to a [Date] object. Specifically, the receiving
 * [String] must also specify both seconds and milliseconds. If the [String] is for some reason
 * not parsable due to incorrect formatting, the resulting [Date] will be null.
 *
 * @return A [Date] object or null if the [String] isn't formatted correctly.
 */
@SuppressLint("SimpleDateFormat")
fun String.fromIso8601(): Date? {
    val timeZone = TimeZone.getTimeZone("UTC")
    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            .apply { this.timeZone = timeZone }

    return try {
        dateFormat.parse(this)
    } catch (e: ParseException) {
        Timber.e(e)
        null
    }
}