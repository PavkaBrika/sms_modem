package com.breakneck.domain.usecase.message

import com.breakneck.domain.model.Message
import com.breakneck.domain.repository.MessageRepository

class GetLastSentMessage(private val messageRepository: MessageRepository) {

    fun execute(): Message {
        return messageRepository.getLastSentMessage()
    }
}