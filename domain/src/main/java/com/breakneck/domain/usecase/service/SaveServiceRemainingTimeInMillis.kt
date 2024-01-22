package com.breakneck.domain.usecase.service

import com.breakneck.domain.repository.ServiceRepository

class SaveServiceRemainingTimeInMillis(private val serviceRepository: ServiceRepository) {

    fun execute(time: Long) {
        serviceRepository.saveServiceRemainingTime(time = time)
    }
}