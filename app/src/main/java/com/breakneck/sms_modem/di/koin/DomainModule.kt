package com.breakneck.sms_modem.di.koin

import com.breakneck.domain.usecase.GetMessageDestinationUrl
import com.breakneck.domain.usecase.GetPort
import com.breakneck.domain.usecase.GetServiceState
import com.breakneck.domain.usecase.SaveMessageDestinationUrl
import com.breakneck.domain.usecase.SavePort
import com.breakneck.domain.usecase.SaveServiceState
import com.breakneck.domain.usecase.SendMessageToServer
import org.koin.dsl.module

val domainModule = module {

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

    factory<GetServiceState> {
        GetServiceState(serviceRepository = get())
    }

    factory<SaveServiceState> {
        SaveServiceState(serviceRepository = get())
    }

    factory<SendMessageToServer> {
        SendMessageToServer(networkRepository = get())
    }

}