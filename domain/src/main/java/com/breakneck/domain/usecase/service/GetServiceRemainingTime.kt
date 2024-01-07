package com.breakneck.domain.usecase.service

import com.breakneck.domain.repository.ServiceRepository
import com.breakneck.domain.repository.SettingsRepository

class GetServiceRemainingTime(private val serviceRepository: ServiceRepository) {

    fun execute(): Long {
        return serviceRepository.getServiceRemainingTime()
    }
}