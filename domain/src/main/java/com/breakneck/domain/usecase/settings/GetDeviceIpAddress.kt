package com.breakneck.domain.usecase.settings

import com.breakneck.domain.model.IpAddress
import com.breakneck.domain.repository.SettingsRepository

class GetDeviceIpAddress(private val settingsRepository: SettingsRepository) {

    fun execute(): IpAddress {
        return settingsRepository.getDeviceIpAddress()
    }
}