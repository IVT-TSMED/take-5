package au.com.invitro.take5.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Formats {
    private val locale = Locale.forLanguageTag("en-AU")

    fun dateTime(millis: Long): String =
        SimpleDateFormat("EEE d MMM yyyy, h:mm a", locale).format(Date(millis))

    fun shortDate(millis: Long): String =
        SimpleDateFormat("d MMM yyyy", locale).format(Date(millis))

    fun fileStamp(millis: Long): String =
        SimpleDateFormat("yyyyMMdd_HHmm", locale).format(Date(millis))

    fun safeFileName(text: String): String =
        text.trim().replace(Regex("[^A-Za-z0-9]+"), "_").trim('_').take(40).ifEmpty { "Site" }

    fun coordinates(lat: Double, lng: Double, accuracy: Float?): String =
        String.format(locale, "%.6f, %.6f", lat, lng) +
            (accuracy?.let { String.format(locale, " (±%.0f m)", it) } ?: "")
}
