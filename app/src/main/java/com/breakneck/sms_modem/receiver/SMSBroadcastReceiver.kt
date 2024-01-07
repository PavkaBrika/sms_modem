package com.breakneck.sms_modem.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import com.breakneck.domain.model.Message
import com.breakneck.domain.model.Sender
import com.breakneck.domain.usecase.settings.GetMessageDestinationUrl
import com.breakneck.domain.usecase.message.SaveSentMessage
import com.breakneck.domain.usecase.message.SendMessageToServer
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class SMSBroadcastReceiver: BroadcastReceiver(), KoinComponent {

    val TAG = "SMSBroadcastReceiver"

    val sendMessageToServer: SendMessageToServer by inject()
    val getMessageDestinationUrl: GetMessageDestinationUrl by inject()
    val saveSentMessage: SaveSentMessage by inject()


    val coroutinesExceptionsHandler = CoroutineExceptionHandler { _, throwable ->
        throwable.printStackTrace()
    }

    fun BroadcastReceiver.goAsync(
        context: CoroutineContext = EmptyCoroutineContext,
        block: suspend CoroutineScope.() -> Unit
    ) {
        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob()).launch(Dispatchers.IO + coroutinesExceptionsHandler) {
            try {
                block()
            } finally {
                pendingResult.finish()
            }
        }
    }


    override fun onReceive(context: Context?, intent: Intent) = goAsync {
        if (context == null || intent == null || intent.action == null)
            return@goAsync
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            return@goAsync
        }
        val smsMessages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        val text: StringBuilder = StringBuilder("")
        var cellNumber = ""
        for (message in smsMessages) {
            text.append(message.messageBody + " ")
            cellNumber = message.displayOriginatingAddress
        }
        Log.e(TAG, "Message from $cellNumber, body $text")
        val message = Message(cellNumber = cellNumber, text = text.toString(), sender = Sender.Phone)
        saveSentMessage.execute(message)
        sendMessageToServer.execute(url = getMessageDestinationUrl.execute(), message = message)
    }
}