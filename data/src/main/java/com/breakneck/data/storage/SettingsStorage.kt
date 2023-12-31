package com.breakneck.data.storage

import com.breakneck.data.entity.MessageDestinationUrlData
import com.breakneck.data.entity.PortData
import com.breakneck.domain.model.MessageDestinationUrl

interface SettingsStorage {

    fun savePort(port: PortData)

    fun getPort(): PortData

    fun saveMessageDestinationUrl(url: MessageDestinationUrlData)

    fun getMessageDestinationUrl(): MessageDestinationUrlData
}