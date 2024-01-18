package com.breakneck.domain.usecase.settings

import com.breakneck.domain.repository.SettingsRepository

class GetRemindNotificationTime(private val settingsRepository: SettingsRepository) {

    fun execute(): Long {
        return settingsRepository.getRemindNotificationTime()
    }
}