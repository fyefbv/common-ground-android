package com.example.common_ground_android.utils

import java.util.Locale

object LocaleUtils {
    fun getCurrentLanguage(): String {
        return when (Locale.getDefault().language){
            "ru" -> "ru"
            else -> "en"
        }
    }
}