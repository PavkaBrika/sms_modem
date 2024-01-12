package com.breakneck.data.storage

import com.breakneck.data.entity.IpAddressData
import com.breakneck.data.entity.MessageDestinationUrlData
import com.breakneck.data.entity.PortData

interface SettingsStorage {

    fun savePort(port: PortData)

    fun getPort(): PortData

    fun saveMessageDestinationUrl(url: MessageDestinationUrlData)

    fun getMessageDestinationUrl(): MessageDestinationUrlData

    fun saveDeviceIpAddress(ipAddress: IpAddressData)

    fun getDeviceIpAddress(): IpAddressData
}