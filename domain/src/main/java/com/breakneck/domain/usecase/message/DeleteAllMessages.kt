package com.breakneck.domain.usecase.message

import com.breakneck.domain.repository.MessageRepository

class DeleteAllMessages(private val messageRepository: MessageRepository) {

    fun execute() {
        messageRepository.deleteAllMessages()
    }
}