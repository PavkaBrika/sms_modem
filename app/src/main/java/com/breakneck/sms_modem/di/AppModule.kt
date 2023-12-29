package com.breakneck.sms_modem.di

import android.content.Context
import com.breakneck.domain.usecase.GetPort
import com.breakneck.domain.usecase.SavePort
//import com.breakneck.sms_modem.viewmodel.MainViewModelFactory
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import javax.inject.Singleton

@Module
class AppModule(val context: Context) {

    @Provides
    fun provideContext(): Context {
        return context
    }

//    @Provides
//    fun provideMainViewModelFactory(
//        savePort: SavePort,
//        getPort: GetPort
//    ): MainViewModelFactory {
//        return MainViewModelFactory(
//            savePort = savePort,
//            getPort = getPort
//        )
//    }
}