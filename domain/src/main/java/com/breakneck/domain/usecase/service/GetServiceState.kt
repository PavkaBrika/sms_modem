package com.breakneck.domain.usecase.service

import com.breakneck.domain.model.ServiceState
import com.breakneck.domain.repository.ServiceRepository

class GetServiceState(private val serviceRepository: ServiceRepository) {

    fun execute(): ServiceState {
        return serviceRepository.getServiceState()
    }
}