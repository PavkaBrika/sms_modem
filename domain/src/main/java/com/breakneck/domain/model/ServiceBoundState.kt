package com.breakneck.domain.model

sealed class ServiceBoundState {
    data object Bounded: ServiceBoundState()
    data object Unbounded: ServiceBoundState()
}