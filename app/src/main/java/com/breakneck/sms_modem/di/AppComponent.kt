package com.breakneck.sms_modem.di

import android.app.Application
import android.content.Context
import com.breakneck.sms_modem.app.App
import com.breakneck.sms_modem.presentation.MainActivity
import com.breakneck.sms_modem.service.NetworkService
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton


@Singleton
@Component(
    modules = [AndroidInjectionModule::class, DomainModule::class, DataModule::class, AppModule::class, ServiceModule::class]
)
interface AppComponent {

    fun inject(app: Application)

    fun inject(mainActivity: MainActivity)

    fun inject(networkService: NetworkService)
}