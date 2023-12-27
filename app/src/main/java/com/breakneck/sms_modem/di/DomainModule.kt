package com.breakneck.sms_modem.di

import com.breakneck.domain.repository.SettingsRepository
import com.breakneck.domain.usecase.GetPort
import com.breakneck.domain.usecase.SavePort
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
}