package com.breakneck.domain.usecase.settings

import com.breakneck.domain.model.DeviceIpAddress
import com.breakneck.domain.repository.SettingsRepository

class GetDeviceIpAddress(private val settingsRepository: SettingsRepository) {

    fun execute(): DeviceIpAddress {
        return settingsRepository.getDeviceIpAddress()
    }
}