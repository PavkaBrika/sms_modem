package com.breakneck.domain.usecase.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FromTimestampToDateString {

    fun execute(timestampMillis: Long, locale: Locale): String {
        val formatter = SimpleDateFormat("h:mm a dd.MM.yyyy")
        return formatter.format(Date(timestampMillis))
    }
}