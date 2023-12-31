package com.breakneck.domain.repository

import com.breakneck.domain.model.Message
import com.breakneck.domain.model.MessageDestinationUrl
import com.breakneck.domain.model.ServiceState

interface NetworkRepository {

    fun sendMessageToServer(url: MessageDestinationUrl, message: Message)

}