package com.rahul.symptoscan.core.utils

import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateUtils {
    private val isoFormatter = DateTimeFormatter.ISO_DATE_TIME
    private val timeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())
    private val dateFormatter = DateTimeFormatter.ofPattern("MMM d", Locale.getDefault())
    private val fullDateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.getDefault())

    fun formatIsoToReadable(isoString: String?): String {
        if (isoString.isNullOrBlank()) return "Recently"
        return try {
            val zonedDateTime = ZonedDateTime.parse(isoString, isoFormatter)
            val localDateTime = zonedDateTime.withZoneSameInstant(java.time.ZoneId.systemDefault())
            val date = localDateTime.toLocalDate()
            val time = localDateTime.toLocalTime()

            val today = LocalDate.now()
            val yesterday = today.minusDays(1)

            val datePart = when (date) {
                today -> "Today"
                yesterday -> "Yesterday"
                else -> {
                    if (date.year == today.year) {
                        date.format(dateFormatter)
                    } else {
                        date.format(fullDateFormatter)
                    }
                }
            }
            
            "$datePart, ${time.format(timeFormatter)}"
        } catch (e: Exception) {
            isoString
        }
    }
}
