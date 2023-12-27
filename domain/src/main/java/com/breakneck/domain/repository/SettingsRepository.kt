package com.breakneck.domain.repository

import com.breakneck.domain.model.Port

interface SettingsRepository {

    fun savePort(port: Port)

    fun getPort(): Port

}