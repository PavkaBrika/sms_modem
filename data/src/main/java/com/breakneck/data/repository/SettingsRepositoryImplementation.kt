package com.breakneck.data.repository

import com.breakneck.data.entity.MessageDestinationUrlData
import com.breakneck.data.entity.PortData
import com.breakneck.data.storage.SettingsStorage
import com.breakneck.domain.model.MessageDestinationUrl
import com.breakneck.domain.model.Port
import com.breakneck.domain.repository.SettingsRepository

class SettingsRepositoryImplementation(private val settingsStorage: SettingsStorage): SettingsRepository {

    override fun savePort(port: Port) {
        settingsStorage.savePort(port = PortData(port.value))
    }

    override fun getPort(): Port {
        return Port(settingsStorage.getPort().value)
    }

    override fun saveMessageDestinationUrl(url: MessageDestinationUrl) {
        settingsStorage.saveMessageDestinationUrl(MessageDestinationUrlData(url = url.url))
    }

    override fun getMessageDestinationUrl(): MessageDestinationUrl {
        return MessageDestinationUrl(url = settingsStorage.getMessageDestinationUrl().url)
    }
}