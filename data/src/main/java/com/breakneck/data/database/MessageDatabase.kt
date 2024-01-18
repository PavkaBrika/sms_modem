package com.breakneck.data.database

import android.content.Context
import android.util.Log
import androidx.room.Room
import com.breakneck.data.entity.MessageData
import com.breakneck.data.storage.DatabaseStorage

const val DATABASE_NAME = "MESSAGE_DATABASE"

class MessageDatabase(private val context: Context): DatabaseStorage {

    val db = Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME).build()

    override fun saveSentMessage(message: MessageData) {
        db.messageDao().insertMessage(message = message)
        Log.e("TAG", "message saved")
    }

    override fun getAllMessages(): List<MessageData> {
        return db.messageDao().getAllMessages()
    }

    override fun updateMessage(message: MessageData) {
        db.messageDao().updateMessage(message = message)
    }

    override fun deleteAllMessages() {
        db.messageDao().deleteAllMessages()
    }
}