package com.breakneck.data.entity

sealed class ServiceStateData {
    object Enabled: ServiceStateData()
    object Disabled: ServiceStateData()
}