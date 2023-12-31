package com.breakneck.domain.usecase

import com.breakneck.domain.model.MessageDestinationUrl
import com.breakneck.domain.repository.SettingsRepository

class SaveMessageDestinationUrl(private val settingsRepository: SettingsRepository) {

    fun execute(url: MessageDestinationUrl) {
        settingsRepository.saveMessageDestinationUrl(url = url)
    }
}