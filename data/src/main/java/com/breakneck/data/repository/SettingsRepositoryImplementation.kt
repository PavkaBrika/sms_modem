package com.breakneck.data.repository

import com.breakneck.data.entity.PortData
import com.breakneck.data.storage.SettingsStorage
import com.breakneck.domain.model.Port
import com.breakneck.domain.repository.SettingsRepository

class SettingsRepositoryImplementation(private val settingsStorage: SettingsStorage): SettingsRepository {

    override fun savePort(port: Port) {
        settingsStorage.savePort(port = PortData(port.value))
    }

    override fun getPort(): Port {
        return Port(settingsStorage.getPort().value)
    }
}