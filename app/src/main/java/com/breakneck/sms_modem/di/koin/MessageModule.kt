package com.breakneck.sms_modem.di.koin

import com.breakneck.domain.usecase.message.DeleteAllMessages
import com.breakneck.domain.usecase.message.GetAllMessages
import com.breakneck.domain.usecase.message.SaveSentMessage
import com.breakneck.domain.usecase.message.SendMessageToServer
import com.breakneck.domain.usecase.message.UpdateMessage
import org.koin.dsl.module

val messageModule = module {

    factory<SendMessageToServer> {
        SendMessageToServer(messageRepository = get())
    }

    factory<SaveSentMessage> {
        SaveSentMessage(messageRepository = get())
    }

    factory<GetAllMessages> {
        GetAllMessages(messageRepository = get())
    }

    factory<UpdateMessage> {
        UpdateMessage(messageRepository = get())
    }

    factory<DeleteAllMessages> {
        DeleteAllMessages(messageRepository = get())
    }

}