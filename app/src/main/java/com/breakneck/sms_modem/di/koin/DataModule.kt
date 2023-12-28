package com.breakneck.sms_modem.di.koin

import com.breakneck.data.repository.ServiceRepositoryImplementation
import com.breakneck.data.repository.SettingsRepositoryImplementation
import com.breakneck.data.sharedpreference.ServiceSharedPreferences
import com.breakneck.data.sharedpreference.SettingsSharedPreference
import com.breakneck.data.storage.ServiceStorage
import com.breakneck.data.storage.SettingsStorage
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


}