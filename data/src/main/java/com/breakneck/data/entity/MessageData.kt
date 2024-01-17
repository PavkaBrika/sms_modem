package com.breakneck.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.breakneck.domain.model.Sender
import java.util.Date

@Entity
data class MessageData(
    @PrimaryKey(autoGenerate = true)
    val id: Long?,
    val cellNumber: String,
    val text: String,
    val date: String,
    val sender: SenderData?,
    val sent: Boolean?
) {
    constructor(cellNumber: String, text: String, date: String) : this(
        id = null,
        cellNumber = cellNumber,
        text = text,
        date = date,
        sender = null,
        sent = null
    )

    constructor(id: Long?, cellNumber: String, text: String, date: String, sender: SenderData?) : this(
        id = null,
        cellNumber = cellNumber,
        text = text,
        date = date,
        sender = sender,
        sent = null
    )
}