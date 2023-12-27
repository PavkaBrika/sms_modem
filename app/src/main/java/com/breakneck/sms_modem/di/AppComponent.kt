package com.breakneck.sms_modem.di

import com.breakneck.sms_modem.presentation.MainActivity
import dagger.Component

@Component(modules = [DomainModule::class, DataModule::class, AppModule::class])
interface AppComponent {

    fun inject(mainActivity: MainActivity)
}