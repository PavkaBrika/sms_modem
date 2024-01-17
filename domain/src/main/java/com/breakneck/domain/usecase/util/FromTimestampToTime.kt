package com.breakneck.domain.usecase.util

import java.text.SimpleDateFormat

class FromTimestampToTime {

    var sdf = SimpleDateFormat("HH:mm:ss")

    fun execute(timestamp: Long): String {
        return sdf.format(timestamp)
    }
}