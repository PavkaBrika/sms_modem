package com.breakneck.data.storage

import com.breakneck.data.entity.PortData

interface SettingsStorage {

    fun savePort(port: PortData)

    fun getPort(): PortData
}