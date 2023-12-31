package com.breakneck.sms_modem.app

import android.app.Application
import android.app.Service
import com.breakneck.sms_modem.di.AppComponent
import com.breakneck.sms_modem.di.AppModule
import com.breakneck.sms_modem.di.koin.appModule
import com.breakneck.sms_modem.di.koin.dataModule
import com.breakneck.sms_modem.di.koin.domainModule
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import javax.inject.Inject

//class App : Application(), HasAndroidInjector {
//
//    lateinit var appComponent: AppComponent
//
//    @Inject
//    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>
//
//    override fun onCreate() {
//        super.onCreate()
//
//        appComponent = DaggerAppComponent
//            .builder()
//            .appModule(AppModule(context = this))
//            .build()
//        appComponent.inject(this)
//
//    }
//
//    override fun androidInjector(): AndroidInjector<Any> {
//        return dispatchingAndroidInjector
//    }
//}

//TODO implement dagger instead koin
class App : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger(level = Level.DEBUG)
            androidContext(this@App)
            modules(listOf(domainModule, dataModule, appModule))
        }

    }
}