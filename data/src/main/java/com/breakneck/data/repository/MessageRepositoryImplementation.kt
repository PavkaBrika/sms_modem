package com.breakneck.data.repository

import com.breakneck.data.entity.MessageData
import com.breakneck.data.entity.MessageDestinationUrlData
import com.breakneck.data.storage.DatabaseStorage
import com.breakneck.data.storage.NetworkStorage
import com.breakneck.domain.model.Message
import com.breakneck.domain.model.MessageDestinationUrl
import com.breakneck.domain.repository.MessageRepository

class MessageRepositoryImplementation(
    private val networkStorage: NetworkStorage,
    private val databaseStorage: DatabaseStorage
) : MessageRepository {

    override fun sendMessageToServer(url: MessageDestinationUrl, message: Message) {
        networkStorage.sendMessageToServer(
            url = MessageDestinationUrlData(url = url.url),
            message = MessageData(cellNumber = message.cellNumber, text = message.text)
        )
    }

    override fun saveSentMessage(message: Message) {
        databaseStorage.saveSentMessage(
            message = MessageData(
                cellNumber = message.cellNumber,
                text = message.text,
                sender = "",
                id = 0
            )
        )
    }

    override fun getLastSentMessage(): Message {
        databaseStorage.getLastSentMessage().also {
            return Message(cellNumber = it.cellNumber, text = it.text)
        }
    }
}