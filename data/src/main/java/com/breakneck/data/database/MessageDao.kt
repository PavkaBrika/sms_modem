package com.breakneck.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.breakneck.data.entity.MessageData

@Dao
interface MessageDao {

    @Query("SELECT * FROM messagedata")
    fun getAllMessages(): List<MessageData>


    @Query("SELECT * FROM messagedata ORDER BY id DESC LIMIT 1")
    fun getLastMessage(): MessageData

    @Insert
    fun insertMessage(message: MessageData)

    @Delete
    fun deleteMessage(message: MessageData)

    @Update
    fun updateMessage(message: MessageData)

}