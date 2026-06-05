package com.example.common_ground_android.utils

import android.content.Context
import androidx.annotation.StringRes

object Res {
    lateinit var context: Context

    fun init(context: Context) {
        this.context = context.applicationContext
    }

    fun getString(@StringRes id: Int): String = context.getString(id)
}