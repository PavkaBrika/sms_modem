package com.breakneck.domain.repository

import com.breakneck.domain.model.ServiceState

interface ServiceRepository {

    fun saveServiceState(serviceState: ServiceState)

    fun getServiceState(): ServiceState

}