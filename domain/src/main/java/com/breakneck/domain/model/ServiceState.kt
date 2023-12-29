package com.breakneck.domain.model

import java.io.Serializable

sealed class ServiceState {
    data object Enabled : ServiceState()
    data object Disabled : ServiceState()
}