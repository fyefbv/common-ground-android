package com.example.common_ground_android.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    private const val ISO_8601_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'"
    private const val ISO_8601_FORMAT_WITH_MILLIS = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
    private const val ISO_8601_FORMAT_OFFSET = "yyyy-MM-dd'T'HH:mm:ssXXX"
    private const val ISO_8601_FORMAT_WITH_MILLIS_OFFSET = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX"

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

    private val isoFormatterWithOffset: SimpleDateFormat by lazy {
        SimpleDateFormat(ISO_8601_FORMAT_OFFSET, Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    }

    private val isoFormatterWithMillisAndOffset: SimpleDateFormat by lazy {
        SimpleDateFormat(ISO_8601_FORMAT_WITH_MILLIS_OFFSET, Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    }

    fun parseIsoDate(isoString: String): Date {
        return try {
            isoFormatterWithOffset.parse(isoString) ?: Date()
        } catch (e: Exception) {
            try {
                isoFormatterWithMillisAndOffset.parse(isoString) ?: Date()
            } catch (e2: Exception) {
                try {
                    isoFormatter.parse(isoString) ?: Date()
                } catch (e3: Exception) {
                    try {
                        isoFormatterWithMillis.parse(isoString) ?: Date()
                    } catch (e4: Exception) {
                        Date()
                    }
                }
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

    fun formatDate(date: Date): String {
        val formatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        return formatter.format(date)
    }
}