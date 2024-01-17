package com.breakneck.domain.usecase.message

import com.breakneck.domain.model.Message
import com.breakneck.domain.repository.MessageRepository

class UpdateMessage(private val messageRepository: MessageRepository) {

    fun execute(message: Message) {
        messageRepository.updateMessage(message = message)
    }
}