package com.breakneck.data.storage

import com.breakneck.data.entity.DeviceIpAddressData
import com.breakneck.data.entity.MessageDestinationUrlData
import com.breakneck.data.entity.PortData
import com.breakneck.domain.model.DeviceIpAddress
import com.breakneck.domain.model.MessageDestinationUrl

interface SettingsStorage {

    fun savePort(port: PortData)

    fun getPort(): PortData

    fun saveMessageDestinationUrl(url: MessageDestinationUrlData)

    fun getMessageDestinationUrl(): MessageDestinationUrlData

    fun saveDeviceIpAddress(ipAddress: DeviceIpAddressData)

    fun getDeviceIpAddress(): DeviceIpAddressData
}