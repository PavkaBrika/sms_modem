package com.breakneck.data.database

import android.content.Context
import androidx.room.Room
import com.breakneck.data.entity.MessageData
import com.breakneck.data.storage.DatabaseStorage

const val DATABASE_NAME = "MESSAGE_DATABASE"

class MessageDatabase(private val context: Context): DatabaseStorage {

    val db = Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME).build()

    override fun saveSentMessage(message: MessageData) {
        db.messageDao().insertMessage(message = message)
    }

    override fun getLastSentMessage(): MessageData {
        return db.messageDao().getLastMessage()
    }

    override fun getAllMessages(): List<MessageData> {
        return db.messageDao().getAllMessages()
    }
}