package com.breakneck.domain.model

sealed class NetworkState {
    data object Available: NetworkState()
    data object Unavailable: NetworkState()
}