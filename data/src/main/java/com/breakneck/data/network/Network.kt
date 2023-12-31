package com.breakneck.data.network

import com.breakneck.data.entity.MessageData
import com.breakneck.data.entity.MessageDestinationUrlData
import com.breakneck.data.storage.NetworkStorage

class Network(private val networkApi: NetworkApi): NetworkStorage {

    override fun sendMessageToServer(url: MessageDestinationUrlData, message: MessageData) {
        networkApi.pushMessage(
            url = url.url,
            message.cellNumber,
            message.text
        )
    }
}