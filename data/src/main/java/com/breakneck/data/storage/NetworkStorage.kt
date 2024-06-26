package com.breakneck.data.storage

import com.breakneck.data.entity.MessageData
import com.breakneck.data.entity.MessageDestinationUrlData

interface NetworkStorage {

    suspend fun sendMessageToServer(url: MessageDestinationUrlData, message: MessageData)

}