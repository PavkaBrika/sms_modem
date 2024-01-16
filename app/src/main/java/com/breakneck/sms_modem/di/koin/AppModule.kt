package com.breakneck.sms_modem.di.koin

import android.util.Log
import com.breakneck.data.network.NetworkApi
import com.breakneck.sms_modem.viewmodel.MainActivityViewModel
import com.breakneck.sms_modem.viewmodel.MainFragmentViewModel
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val appModule = module {

    viewModel<MainActivityViewModel> {
        MainActivityViewModel(
            savePort = get(),
            getPort = get(),
            getServiceState = get(),
            saveMessageDestinationUrl = get(),
            getMessageDestinationUrl = get(),
            getAllMessages = get(),
            saveServiceRemainingTime = get(),
            getServiceRemainingTime = get(),
            getDeviceIpAddress = get()
        )
    }

    viewModel<MainFragmentViewModel> {
        MainFragmentViewModel()
    }

    single<NetworkApi> {
        val httpLoggingInterceptor = HttpLoggingInterceptor()
        httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

        val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(httpLoggingInterceptor)
                .build()

        val retrofit = Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl("https://helloworld")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        Log.e("AppModule", "Retrofit created")
        return@single retrofit.create(NetworkApi::class.java)
    }
}