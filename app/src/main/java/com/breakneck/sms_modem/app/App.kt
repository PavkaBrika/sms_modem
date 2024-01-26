package com.breakneck.sms_modem.app

import android.app.Application
import android.util.Log
import com.appodeal.ads.Appodeal
import com.appodeal.ads.initializing.ApdInitializationCallback
import com.appodeal.ads.initializing.ApdInitializationError
import com.breakneck.sms_modem.di.koin.appModule
import com.breakneck.sms_modem.di.koin.dataModule
import com.breakneck.sms_modem.di.koin.messageModule
import com.breakneck.sms_modem.di.koin.serviceModule
import com.breakneck.sms_modem.di.koin.settingsModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class App : Application() {

    private val TAG = "Application"

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger(level = Level.DEBUG)
            androidContext(this@App)
            modules(listOf(settingsModule, serviceModule, messageModule, dataModule, appModule))
        }

        Appodeal.initialize(
            this,
            "95674fa4452fb4e96f117ad34caddd0409a299382b5e43d5",
            Appodeal.BANNER or Appodeal.REWARDED_VIDEO,
            object : ApdInitializationCallback {
                override fun onInitializationFinished(errors: List<ApdInitializationError>?) {
                    Log.e(TAG, "Appodeal initialized")
                }
            })

    }
}