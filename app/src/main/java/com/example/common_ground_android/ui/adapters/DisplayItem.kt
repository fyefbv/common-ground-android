package com.example.common_ground_android.ui.adapters

import com.example.common_ground_android.network.model.domain.Message
import java.util.Date

sealed class DisplayItem {
    data class Header(val date: Date, val text: String) : DisplayItem()
    data class MessageItem(val message: Message) : DisplayItem()
}