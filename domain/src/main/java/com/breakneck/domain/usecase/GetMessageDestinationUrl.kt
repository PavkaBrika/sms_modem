package com.breakneck.domain.usecase

import com.breakneck.domain.model.MessageDestinationUrl
import com.breakneck.domain.repository.SettingsRepository

class GetMessageDestinationUrl(private val settingsRepository: SettingsRepository) {

    fun execute(): MessageDestinationUrl {
        return settingsRepository.getMessageDestinationUrl()
    }
}