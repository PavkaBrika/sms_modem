package com.breakneck.domain.usecase.service

import com.breakneck.domain.model.ServiceState
import com.breakneck.domain.repository.ServiceRepository

class SaveServiceState(private val serviceRepository: ServiceRepository) {

    fun execute(serviceState: ServiceState) {
        serviceRepository.saveServiceState(serviceState = serviceState)
    }
}