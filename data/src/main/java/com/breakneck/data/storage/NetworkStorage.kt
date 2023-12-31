package com.breakneck.data.storage

import com.breakneck.data.entity.MessageData
import com.breakneck.data.entity.MessageDestinationUrlData
import com.breakneck.domain.model.Message

interface NetworkStorage {

    fun sendMessageToServer(url: MessageDestinationUrlData, message: MessageData)

}