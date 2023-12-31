package com.breakneck.domain.repository

import com.breakneck.domain.model.Message
import com.breakneck.domain.model.MessageDestinationUrl

interface MessageRepository {

    fun sendMessageToServer(url: MessageDestinationUrl, message: Message)

    fun saveSentMessage(message: Message)

    fun getLastSentMessage(): Message

}