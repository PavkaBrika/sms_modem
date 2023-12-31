package com.breakneck.domain.usecase

import com.breakneck.domain.model.Message
import com.breakneck.domain.model.MessageDestinationUrl
import com.breakneck.domain.repository.NetworkRepository

class SendMessageToServer(private val networkRepository: NetworkRepository) {

    fun execute(url: MessageDestinationUrl, message: Message) {
        networkRepository.sendMessageToServer(url = url, message = message)
    }
}