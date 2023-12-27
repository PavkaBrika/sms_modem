package com.breakneck.data.entity

sealed class ServiceStateData {
    object enabled: ServiceStateData()
    object disabled: ServiceStateData()
}