package com.breakneck.sms_modem.di

import android.content.Context
import com.breakneck.data.repository.ServiceRepositoryImplementation
import com.breakneck.data.repository.SettingsRepositoryImplementation
import com.breakneck.data.sharedpreference.ServiceSharedPreferences
import com.breakneck.data.sharedpreference.SettingsSharedPreference
import com.breakneck.data.storage.ServiceStorage
import com.breakneck.data.storage.SettingsStorage
import com.breakneck.domain.repository.ServiceRepository
import com.breakneck.domain.repository.SettingsRepository
import dagger.Module
import dagger.Provides

@Module
class DataModule {

    @Provides
    fun provideSettingsRepository(settingsStorage: SettingsStorage): SettingsRepository {
        return SettingsRepositoryImplementation(settingsStorage = settingsStorage)
    }

    @Provides
    fun provideSettingsStorage(context: Context): SettingsStorage {
        return SettingsSharedPreference(context = context)
    }

    @Provides
    fun provideServiceRepository(serviceStorage: ServiceStorage): ServiceRepository {
        return ServiceRepositoryImplementation(serviceStorage = serviceStorage)
    }

    @Provides
    fun provideServiceStorage(context: Context): ServiceStorage {
        return ServiceSharedPreferences(context = context)
    }
}