package com.breakneck.data.sharedpreference

import android.content.Context
import com.breakneck.data.entity.PortData
import com.breakneck.data.storage.SettingsStorage

const val SETTINGS_SHARED_PREFERENCES_NAME = "SETTINGS_SHARED_PREFERENCES"
const val PORT = "PORT"

class SettingsSharedPreference(private val context: Context): SettingsStorage {

    private val sharedPreference = context.getSharedPreferences(SETTINGS_SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)

    override fun savePort(port: PortData) {
        sharedPreference.edit().putInt(PORT, port.value).apply()
    }

    override fun getPort(): PortData {
        return PortData(sharedPreference.getInt(PORT, 5555))
    }
}