package com.breakneck.domain.repository

import com.breakneck.domain.model.Message
import com.breakneck.domain.model.MessageDestinationUrl

interface MessageRepository {

    suspend fun sendMessageToServer(url: MessageDestinationUrl, message: Message)

    fun saveSentMessage(message: Message)

    fun getAllMessages(): List<Message>

    fun updateMessage(message: Message)

}