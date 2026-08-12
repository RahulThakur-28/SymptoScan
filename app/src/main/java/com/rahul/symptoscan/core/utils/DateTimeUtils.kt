package com.rahul.symptoscan.core.utils

import java.text.SimpleDateFormat
import java.util.*

object DateTimeUtils {
    private val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    private val readableFormat = SimpleDateFormat("dd MMM yyyy • h:mm a", Locale.getDefault())
    private val dateFormatOnly = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    fun formatIsoToReadable(isoString: String?): String {
        if (isoString.isNullOrBlank()) return "N/A"
        return try {
            // Handle different ISO lengths if necessary
            val date = isoFormat.parse(isoString.substring(0, 19))
            readableFormat.format(date ?: Date())
        } catch (e: Exception) {
            isoString // Fallback
        }
    }

    fun formatIsoToDate(isoString: String?): String {
        if (isoString.isNullOrBlank()) return "N/A"
        return try {
            val date = isoFormat.parse(isoString.substring(0, 19))
            dateFormatOnly.format(date ?: Date())
        } catch (e: Exception) {
            isoString
        }
    }
}
