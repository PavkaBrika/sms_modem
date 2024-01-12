package com.breakneck.domain.repository

import com.breakneck.domain.model.DeviceIpAddress
import com.breakneck.domain.model.MessageDestinationUrl
import com.breakneck.domain.model.Port

interface SettingsRepository {

    fun savePort(port: Port)

    fun getPort(): Port

    fun saveMessageDestinationUrl(url: MessageDestinationUrl)

    fun getMessageDestinationUrl(): MessageDestinationUrl

    fun saveDeviceIpAddress(ipAddress: DeviceIpAddress)

    fun getDeviceIpAddress(): DeviceIpAddress

}