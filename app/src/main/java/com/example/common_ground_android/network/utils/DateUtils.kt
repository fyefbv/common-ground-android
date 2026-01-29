package com.example.common_ground_android.network.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    private const val ISO_8601_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'"
    private const val ISO_8601_FORMAT_WITH_MILLIS = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"

    private val isoFormatter: SimpleDateFormat by lazy {
        SimpleDateFormat(ISO_8601_FORMAT, Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    }

    private val isoFormatterWithMillis: SimpleDateFormat by lazy {
        SimpleDateFormat(ISO_8601_FORMAT_WITH_MILLIS, Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    }

    fun parseIsoDate(isoString: String): Date {
        return try {
            isoFormatter.parse(isoString) ?: Date()
        } catch (e: Exception) {
            try {
                isoFormatterWithMillis.parse(isoString) ?: Date()
            } catch (e2: Exception) {
                Date()
            }
        }
    }

    fun parseIsoDateNullable(isoString: String?): Date? {
        return isoString?.let { parseIsoDate(it) }
    }

    fun formatToIso(date: Date): String {
        return isoFormatter.format(date)
    }

    fun formatToLocalString(date: Date): String {
        val localFormatter = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        return localFormatter.format(date)
    }

    fun formatToTime(date: Date): String {
        val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
        return timeFormatter.format(date)
    }
}