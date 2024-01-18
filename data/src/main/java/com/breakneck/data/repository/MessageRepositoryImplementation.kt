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

    override suspend fun sendMessageToServer(url: MessageDestinationUrl, message: Message) {
        networkStorage.sendMessageToServer(
            url = MessageDestinationUrlData(value = url.value),
            message = MessageData(
                cellNumber = message.cellNumber,
                text = message.text,
                date = message.date
            )
        )
    }

    override fun saveSentMessage(message: Message) {
        databaseStorage.saveSentMessage(
            message = MessageData(
                cellNumber = message.cellNumber,
                text = message.text,
                date = message.date,
                sender = when (message.sender) {
                    Sender.Phone -> SenderData.Phone
                    Sender.Server -> SenderData.Server
                    null -> null
                },
                sent = message.sent,
                id = null
            )
        )
    }

    override fun getAllMessages(): List<Message> {
        val messagesDataList = databaseStorage.getAllMessages()
        return messagesDataList.map {
            Message(
                id = it.id,
                cellNumber = it.cellNumber,
                text = it.text,
                date = it.date,
                sender = when (it.sender) {
                    SenderData.Phone -> Sender.Phone
                    SenderData.Server -> Sender.Server
                    else -> null
                },
                sent = it.sent
            )
        }
    }

    override fun updateMessage(message: Message) {
        databaseStorage.updateMessage(
            MessageData(
                id = message.id,
                cellNumber = message.cellNumber,
                text = message.text,
                date = message.date,
                sender = when (message.sender) {
                    Sender.Phone -> SenderData.Phone
                    Sender.Server -> SenderData.Server
                    null -> null
                },
                sent = message.sent
            )
        )
    }

    override fun deleteAllMessages() {
        databaseStorage.deleteAllMessages()
    }
}