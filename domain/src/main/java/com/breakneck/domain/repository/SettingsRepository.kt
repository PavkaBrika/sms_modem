package com.breakneck.domain.repository

import com.breakneck.domain.model.IpAddress
import com.breakneck.domain.model.MessageDestinationUrl
import com.breakneck.domain.model.Port
import com.breakneck.domain.model.RemainingAdsQuantity

interface SettingsRepository {

    fun savePort(port: Port)

    fun getPort(): Port

    fun saveMessageDestinationUrl(url: MessageDestinationUrl)

    fun getMessageDestinationUrl(): MessageDestinationUrl

    fun saveDeviceIpAddress(ipAddress: IpAddress)

    fun getDeviceIpAddress(): IpAddress

    fun getRemindNotificationTime(): Long

    fun saveRemindNotificationTime(timeInMillis: Long)

    fun getRemainingAdsQuantity(): RemainingAdsQuantity

    fun saveRemainingAdsQuantity(quantity: RemainingAdsQuantity)

    fun getIsFirstTimeAppOpened(): Boolean

    fun saveIsFirstTimeAppOpened(isOpened: Boolean)
}