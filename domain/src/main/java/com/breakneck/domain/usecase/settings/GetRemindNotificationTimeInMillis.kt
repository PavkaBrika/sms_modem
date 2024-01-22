package com.breakneck.domain.usecase.settings

import com.breakneck.domain.repository.SettingsRepository

class GetRemindNotificationTimeInMillis(private val settingsRepository: SettingsRepository) {

    fun execute(): Long {
        return settingsRepository.getRemindNotificationTime()
    }
}