package com.breakneck.domain.usecase.settings

import com.breakneck.domain.model.Port
import com.breakneck.domain.repository.SettingsRepository

class GetPort(private val settingsRepository: SettingsRepository) {

    fun execute(): Port {
        return settingsRepository.getPort()
    }
}