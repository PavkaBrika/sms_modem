package com.breakneck.sms_modem.di

import com.breakneck.domain.repository.ServiceRepository
import com.breakneck.domain.repository.SettingsRepository
import com.breakneck.domain.usecase.settings.GetPort
import com.breakneck.domain.usecase.service.GetServiceState
import com.breakneck.domain.usecase.settings.SavePort
import com.breakneck.domain.usecase.service.SaveServiceState
import dagger.Module
import dagger.Provides

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