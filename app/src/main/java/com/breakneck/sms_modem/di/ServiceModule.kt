package com.breakneck.sms_modem.di

import com.breakneck.sms_modem.service.NetworkService
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector

@Module
abstract class ServiceModule {

    @ContributesAndroidInjector
    abstract fun contributeNetworkService(): NetworkService

}