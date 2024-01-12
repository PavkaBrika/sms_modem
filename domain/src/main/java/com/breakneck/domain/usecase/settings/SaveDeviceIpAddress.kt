package com.breakneck.domain.usecase.settings

import com.breakneck.domain.model.IpAddress
import com.breakneck.domain.repository.SettingsRepository

class SaveDeviceIpAddress(private val settingsRepository: SettingsRepository) {

    fun execute(ipAddress: IpAddress) {
        settingsRepository.saveDeviceIpAddress(ipAddress = ipAddress)
    }
}