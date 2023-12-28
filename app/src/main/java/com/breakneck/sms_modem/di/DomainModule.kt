package com.breakneck.sms_modem.di

import com.breakneck.domain.repository.ServiceRepository
import com.breakneck.domain.repository.SettingsRepository
import com.breakneck.domain.usecase.GetPort
import com.breakneck.domain.usecase.GetServiceState
import com.breakneck.domain.usecase.SavePort
import com.breakneck.domain.usecase.SaveServiceState
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class DomainModule {

    @Provides
    fun provideSavePort(settingsRepository: SettingsRepository): SavePort {
        return SavePort(settingsRepository = settingsRepository)
    }

    @Provides
    fun provideGetPort(settingsRepository: SettingsRepository): GetPort {
        return GetPort(settingsRepository = settingsRepository)
    }

    @Provides
    fun provideGetServiceState(serviceRepository: ServiceRepository): GetServiceState {
        return GetServiceState(serviceRepository = serviceRepository)
    }

    @Provides
    fun provideSaveServiceState(serviceRepository: ServiceRepository): SaveServiceState {
        return SaveServiceState(serviceRepository = serviceRepository)
    }
}