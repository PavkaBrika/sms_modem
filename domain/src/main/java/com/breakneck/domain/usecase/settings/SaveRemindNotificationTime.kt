package com.breakneck.domain.usecase.settings

import com.breakneck.domain.repository.SettingsRepository

class SaveRemindNotificationTime(private val settingsRepository: SettingsRepository) {

    fun execute(timeInMillis: Long) {
        settingsRepository.saveRemindNotificationTime(timeInMillis = timeInMillis)
    }
}