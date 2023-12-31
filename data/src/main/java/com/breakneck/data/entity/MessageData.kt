package com.breakneck.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class MessageData(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val cellNumber: String,
    val text: String,
    val sender: SenderData
)