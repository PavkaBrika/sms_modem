package com.breakneck.domain.usecase.settings

import com.breakneck.domain.repository.SettingsRepository

class GetIsFirstTimeAppOpened(private val settingsRepository: SettingsRepository) {

    fun execute(): Boolean {
        return settingsRepository.getIsFirstTimeAppOpened()
    }
}