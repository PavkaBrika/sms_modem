package com.breakneck.sms_modem.di.koin

import com.breakneck.domain.usecase.settings.GetMessageDestinationUrl
import com.breakneck.domain.usecase.settings.GetPort
import com.breakneck.domain.usecase.settings.SaveMessageDestinationUrl
import com.breakneck.domain.usecase.settings.SavePort
import org.koin.dsl.module

val settingsModule = module {

    factory<SavePort> {
        SavePort(settingsRepository = get())
    }

    factory<GetPort> {
        GetPort(settingsRepository = get())
    }

    factory<GetMessageDestinationUrl> {
        GetMessageDestinationUrl(settingsRepository = get())
    }

    factory<SaveMessageDestinationUrl> {
        SaveMessageDestinationUrl(settingsRepository = get())
    }

}