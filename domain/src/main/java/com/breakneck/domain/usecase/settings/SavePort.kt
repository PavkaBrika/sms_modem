package com.breakneck.domain.usecase.settings

import com.breakneck.domain.model.Port
import com.breakneck.domain.repository.SettingsRepository

class SavePort(private val settingsRepository: SettingsRepository) {

    fun execute(port: Port) {
        settingsRepository.savePort(port = port)
    }
}