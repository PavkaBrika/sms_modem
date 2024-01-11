package com.breakneck.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity
data class MessageData(
    @PrimaryKey(autoGenerate = true)
    val id: Long?,
    val cellNumber: String,
    val text: String,
    val date: String,
    val sender: SenderData?
) {
//    constructor(cellNumber: String, text: String, sender: SenderData?) : this(
//        id = null,
//        cellNumber = cellNumber,
//        text = text,
//        sender = sender
//    )
    constructor(cellNumber: String, text: String, date: String) : this(
        id = null,
        cellNumber = cellNumber,
        text = text,
        date = date,
        sender = null
    )
}