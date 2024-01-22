package com.breakneck.domain.usecase.settings

import com.breakneck.domain.repository.SettingsRepository

class SaveIsFirstTimeAppOpened(private val settingsRepository: SettingsRepository) {

    fun execute() {
        settingsRepository.saveIsFirstTimeAppOpened(isOpened = true)
    }
}