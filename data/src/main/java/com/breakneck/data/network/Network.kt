package com.breakneck.data.network

import com.breakneck.data.entity.MessageData
import com.breakneck.data.entity.MessageDestinationUrlData
import com.breakneck.data.storage.NetworkStorage

class Network(private val networkApi: NetworkApi): NetworkStorage {

    override suspend fun sendMessageToServer(url: MessageDestinationUrlData, message: MessageData) {
        networkApi.pushMessage(
            url = url.value,
            message.cellNumber,
            message.text
        )
    }
}