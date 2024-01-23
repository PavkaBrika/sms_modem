package com.breakneck.data.storage

import com.breakneck.data.entity.MessageData

interface DatabaseStorage {

    fun saveSentMessage(message: MessageData)

    fun getAllMessages(): List<MessageData>

    fun updateMessage(message: MessageData)

    fun deleteAllMessages()
}