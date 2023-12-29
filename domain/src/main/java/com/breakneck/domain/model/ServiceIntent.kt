package com.breakneck.domain.model

import java.io.Serializable

sealed class ServiceIntent : Serializable {
    data object Enable: ServiceIntent()
    data object Disable: ServiceIntent()
}