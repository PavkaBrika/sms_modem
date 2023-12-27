package com.breakneck.sms_modem.app

import android.app.Application
import com.breakneck.sms_modem.di.AppComponent
import com.breakneck.sms_modem.di.AppModule
import com.breakneck.sms_modem.di.DaggerAppComponent

class App : Application() {

    lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()

        appComponent = DaggerAppComponent
            .builder()
            .appModule(AppModule(context = this))
            .build()
    }
}