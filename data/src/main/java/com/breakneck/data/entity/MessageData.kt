package com.breakneck.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class MessageData(
    @PrimaryKey(autoGenerate = true)
    val id: Long?,
    val cellNumber: String,
    val text: String,
    val sender: String?
) {
    constructor(cellNumber: String, text: String) : this(
        id = null,
        cellNumber = cellNumber,
        text = text,
        sender = null
    )
}