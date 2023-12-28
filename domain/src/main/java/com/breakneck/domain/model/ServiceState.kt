package com.breakneck.domain.model

import java.io.Serializable

sealed class ServiceState: Serializable {
    data object Enabled : ServiceState()
    data object Disabled : ServiceState()
}