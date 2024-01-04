package com.breakneck.data.repository

import com.breakneck.data.entity.MessageData
import com.breakneck.data.entity.MessageDestinationUrlData
import com.breakneck.data.entity.SenderData
import com.breakneck.data.storage.DatabaseStorage
import com.breakneck.data.storage.NetworkStorage
import com.breakneck.domain.model.Message
import com.breakneck.domain.model.MessageDestinationUrl
import com.breakneck.domain.model.Sender
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
                sender = when (message.sender) {
                    Sender.Phone -> SenderData.Phone
                    Sender.Server -> SenderData.Server
                    null -> null
                },
                id = null
            )
        )
    }

    override fun getLastSentMessage(): Message {
        databaseStorage.getLastSentMessage().also {
            return Message(cellNumber = it.cellNumber, text = it.text)
        }
    }

    override fun getAllMessages(): List<Message> {
        val messagesDataList = databaseStorage.getAllMessages()
        return messagesDataList.map {
            Message(
                cellNumber = it.cellNumber,
                text = it.text,
                sender = when (it.sender) {
                    SenderData.Phone -> Sender.Phone
                    SenderData.Server -> Sender.Server
                    else -> null
                }
            )
        }
    }
}