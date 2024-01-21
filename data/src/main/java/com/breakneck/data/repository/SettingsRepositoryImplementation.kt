package com.breakneck.data.repository

import com.breakneck.data.entity.IpAddressData
import com.breakneck.data.entity.MessageDestinationUrlData
import com.breakneck.data.entity.PortData
import com.breakneck.data.entity.RemainingAdsQuantityData
import com.breakneck.data.storage.SettingsStorage
import com.breakneck.domain.model.IpAddress
import com.breakneck.domain.model.MessageDestinationUrl
import com.breakneck.domain.model.Port
import com.breakneck.domain.model.RemainingAdsQuantity
import com.breakneck.domain.repository.SettingsRepository

class SettingsRepositoryImplementation(private val settingsStorage: SettingsStorage): SettingsRepository {

    override fun savePort(port: Port) {
        settingsStorage.savePort(port = PortData(port.value))
    }

    override fun getPort(): Port {
        return Port(settingsStorage.getPort().value)
    }

    override fun saveMessageDestinationUrl(url: MessageDestinationUrl) {
        settingsStorage.saveMessageDestinationUrl(MessageDestinationUrlData(value = url.value))
    }

    override fun getMessageDestinationUrl(): MessageDestinationUrl {
        return MessageDestinationUrl(value = settingsStorage.getMessageDestinationUrl().value)
    }

    override fun saveDeviceIpAddress(ipAddress: IpAddress) {
        settingsStorage.saveDeviceIpAddress(IpAddressData(ipAddress.value))
    }

    override fun getDeviceIpAddress(): IpAddress {
        return IpAddress(value = settingsStorage.getDeviceIpAddress().value)
    }

    override fun saveRemindNotificationTime(timeInMillis: Long) {
        settingsStorage.saveRemindNotificationTime(timeInMillis = timeInMillis)
    }

    override fun getRemindNotificationTime(): Long {
        return settingsStorage.getRemindNotificationTime()
    }

    override fun saveRemainingAdsQuantity(quantity: RemainingAdsQuantity) {
        settingsStorage.saveRemainingAdsQuantity(quantity = RemainingAdsQuantityData(value = quantity.value))
    }

    override fun getRemainingAdsQuantity(): RemainingAdsQuantity {
        return RemainingAdsQuantity(value = settingsStorage.getRemainingAdsQuantity().value)
    }
}