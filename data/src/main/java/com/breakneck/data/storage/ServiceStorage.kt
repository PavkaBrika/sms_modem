package com.breakneck.data.storage

import com.breakneck.data.entity.ServiceStateData

interface ServiceStorage {

    fun saveServiceState(serviceState: ServiceStateData)

    fun getServiceState(): ServiceStateData

    fun getServiceRemainingTime(): Long

    fun saveServiceRemainingTime(time: Long)
}