package com.breakneck.data.sharedpreference

import android.content.Context
import com.breakneck.data.entity.IpAddressData
import com.breakneck.data.entity.MessageDestinationUrlData
import com.breakneck.data.entity.PortData
import com.breakneck.data.storage.SettingsStorage

const val SETTINGS_SHARED_PREFERENCES_NAME = "SETTINGS_SHARED_PREFERENCES"
const val PORT = "PORT"
const val MESSAGE_DESTINATION_URL = "MESSAGE_DESTINATION_URL"
const val DEVICE_IP_ADDRESS = "DEVICE_IP_ADDRESS"

class SettingsSharedPreference(private val context: Context): SettingsStorage {

    private val sharedPreference = context.getSharedPreferences(SETTINGS_SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)

    override fun savePort(port: PortData) {
        sharedPreference.edit().putInt(PORT, port.value).apply()
    }

    override fun getPort(): PortData {
        return PortData(sharedPreference.getInt(PORT, 5555))
    }

    override fun saveMessageDestinationUrl(url: MessageDestinationUrlData) {
        sharedPreference.edit().putString(MESSAGE_DESTINATION_URL, url.value).apply()
    }

    override fun getMessageDestinationUrl(): MessageDestinationUrlData {
        return MessageDestinationUrlData(value = sharedPreference.getString(MESSAGE_DESTINATION_URL, "")!!)
    }

    override fun saveDeviceIpAddress(ipAddress: IpAddressData) {
        sharedPreference.edit().putString(DEVICE_IP_ADDRESS, ipAddress.value).apply()
    }

    override fun getDeviceIpAddress(): IpAddressData {
        return IpAddressData(value = sharedPreference.getString(DEVICE_IP_ADDRESS, "")!!)
    }
}