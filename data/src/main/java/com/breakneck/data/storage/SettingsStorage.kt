package com.breakneck.data.storage

import com.breakneck.data.entity.IpAddressData
import com.breakneck.data.entity.MessageDestinationUrlData
import com.breakneck.data.entity.PortData
import com.breakneck.data.entity.RemainingAdsQuantityData

interface SettingsStorage {

    fun savePort(port: PortData)

    fun getPort(): PortData

    fun saveMessageDestinationUrl(url: MessageDestinationUrlData)

    fun getMessageDestinationUrl(): MessageDestinationUrlData

    fun saveDeviceIpAddress(ipAddress: IpAddressData)

    fun getDeviceIpAddress(): IpAddressData

    fun getRemindNotificationTime(): Long

    fun saveRemindNotificationTime(timeInMillis: Long)

    fun getRemainingAdsQuantity(): RemainingAdsQuantityData

    fun saveRemainingAdsQuantity(quantity: RemainingAdsQuantityData)

    fun getIsFirstTimeAppOpened(): Boolean

    fun saveIsFirstTimeAppOpened(isOpened: Boolean)
}