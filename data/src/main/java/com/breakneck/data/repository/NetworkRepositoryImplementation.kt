package com.breakneck.data.repository

import com.breakneck.data.entity.MessageData
import com.breakneck.data.entity.MessageDestinationUrlData
import com.breakneck.data.storage.NetworkStorage
import com.breakneck.domain.model.Message
import com.breakneck.domain.model.MessageDestinationUrl
import com.breakneck.domain.repository.NetworkRepository

class NetworkRepositoryImplementation(private val networkStorage: NetworkStorage): NetworkRepository {

    override fun sendMessageToServer(url: MessageDestinationUrl, message: Message) {
        networkStorage.sendMessageToServer(url = MessageDestinationUrlData(url = url.url), message = MessageData(cellNumber = message.cellNumber, text = message.text))
    }

}