package com.breakneck.domain.usecase.message

import com.breakneck.domain.model.Message
import com.breakneck.domain.model.MessageDestinationUrl
import com.breakneck.domain.repository.MessageRepository

class SendMessageToServer(private val messageRepository: MessageRepository) {

    fun execute(url: MessageDestinationUrl, message: Message) {
        messageRepository.sendMessageToServer(url = url, message = message)
    }
}