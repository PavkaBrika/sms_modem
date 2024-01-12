package com.breakneck.domain.usecase.settings

import com.breakneck.domain.model.DeviceIpAddress
import com.breakneck.domain.repository.SettingsRepository

class SaveDeviceIpAddress(private val settingsRepository: SettingsRepository) {

    fun execute(ipAddress: DeviceIpAddress) {
        settingsRepository.saveDeviceIpAddress(ipAddress = ipAddress)
    }
}