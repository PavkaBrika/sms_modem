package com.breakneck.domain.util

sealed class ServiceState {
    object enabled: ServiceState()
    object disabled: ServiceState()
}