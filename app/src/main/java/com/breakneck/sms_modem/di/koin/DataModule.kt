package com.breakneck.sms_modem.di.koin

import com.breakneck.data.database.MessageDatabase
import com.breakneck.data.network.Network
import com.breakneck.data.repository.MessageRepositoryImplementation
import com.breakneck.data.repository.ServiceRepositoryImplementation
import com.breakneck.data.repository.SettingsRepositoryImplementation
import com.breakneck.data.sharedpreference.ServiceSharedPreferences
import com.breakneck.data.sharedpreference.SettingsSharedPreference
import com.breakneck.data.storage.DatabaseStorage
import com.breakneck.data.storage.NetworkStorage
import com.breakneck.data.storage.ServiceStorage
import com.breakneck.data.storage.SettingsStorage
import com.breakneck.domain.repository.MessageRepository
import com.breakneck.domain.repository.ServiceRepository
import com.breakneck.domain.repository.SettingsRepository
import org.koin.dsl.module

val dataModule = module {

    single<SettingsStorage> {
        SettingsSharedPreference(context = get())
    }

    single<SettingsRepository> {
        SettingsRepositoryImplementation(settingsStorage = get())
    }

    single<ServiceStorage> {
        ServiceSharedPreferences(context = get())
    }

    single<ServiceRepository> {
        ServiceRepositoryImplementation(serviceStorage = get())
    }

    single<NetworkStorage> {
        Network(networkApi = get())
    }

    single<MessageRepository> {
        MessageRepositoryImplementation(networkStorage = get(), databaseStorage = get())
    }

    single<DatabaseStorage> {
        MessageDatabase(context = get())
    }

}