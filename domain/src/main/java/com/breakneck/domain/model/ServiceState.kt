package com.breakneck.domain.model

sealed class ServiceState {
    object enabled: ServiceState()
    object disabled: ServiceState()
}