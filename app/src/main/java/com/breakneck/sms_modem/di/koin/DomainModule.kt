package com.breakneck.sms_modem.di.koin

import com.breakneck.domain.usecase.message.GetAllMessages
import com.breakneck.domain.usecase.settings.GetMessageDestinationUrl
import com.breakneck.domain.usecase.settings.GetPort
import com.breakneck.domain.usecase.service.GetServiceState
import com.breakneck.domain.usecase.settings.SaveMessageDestinationUrl
import com.breakneck.domain.usecase.settings.SavePort
import com.breakneck.domain.usecase.message.SaveSentMessage
import com.breakneck.domain.usecase.service.SaveServiceState
import com.breakneck.domain.usecase.message.SendMessageToServer
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
        SendMessageToServer(messageRepository = get())
    }

    factory<SaveSentMessage> {
        SaveSentMessage(messageRepository = get())
    }

    factory<GetAllMessages> {
        GetAllMessages(messageRepository = get())
    }

}