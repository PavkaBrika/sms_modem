package com.breakneck.data.repository

import com.breakneck.data.entity.ServiceStateData
import com.breakneck.data.storage.ServiceStorage
import com.breakneck.domain.model.ServiceState
import com.breakneck.domain.repository.ServiceRepository

class ServiceRepositoryImplementation(private val serviceStorage: ServiceStorage): ServiceRepository {

    override fun saveServiceState(serviceState: ServiceState) {
        serviceStorage.saveServiceState(
            when (serviceState) {
                ServiceState.Disabled -> ServiceStateData.disabled
                ServiceState.Enabled -> ServiceStateData.enabled
            }
        )
    }

    override fun getServiceState(): ServiceState {
        return when (serviceStorage.getServiceState()) {
            ServiceStateData.disabled -> ServiceState.Disabled
            ServiceStateData.enabled -> ServiceState.Enabled
        }
    }
}