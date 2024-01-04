package com.breakneck.data.database

import androidx.room.TypeConverter
import com.breakneck.data.entity.SenderData

class Converters {
    @TypeConverter
    fun fromStringToSender(sender: String): SenderData {
        return when (sender) {
            "phone" -> SenderData.Phone
            "server" -> SenderData.Server
            else -> SenderData.Phone
        }
    }

    @TypeConverter
    fun senderToString(sender: SenderData): String {
        return when (sender) {
            SenderData.Phone -> "phone"
            SenderData.Server -> "server"
        }
    }
}