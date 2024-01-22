package com.breakneck.domain.usecase.service

import com.breakneck.domain.repository.ServiceRepository

class GetServiceRemainingTimeInMillis(private val serviceRepository: ServiceRepository) {

    fun execute(): Long {
        return serviceRepository.getServiceRemainingTime()
    }
}