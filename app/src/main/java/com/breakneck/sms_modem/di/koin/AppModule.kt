package com.breakneck.sms_modem.di.koin

import com.breakneck.sms_modem.viewmodel.MainViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    viewModel<MainViewModel> {
        MainViewModel(
            savePort = get(),
            getPort = get()
        )
    }
}