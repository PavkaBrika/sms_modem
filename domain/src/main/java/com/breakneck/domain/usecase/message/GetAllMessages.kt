package com.breakneck.domain.usecase.message

import com.breakneck.domain.model.Message
import com.breakneck.domain.repository.MessageRepository

class GetAllMessages(private val messageRepository: MessageRepository) {

    fun execute(): List<Message> {
        return messageRepository.getAllMessages()
    }
}