package com.breakneck.sms_modem.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Telephony
import android.util.Log
import com.breakneck.domain.model.Message
import com.breakneck.domain.model.Sender
import com.breakneck.domain.usecase.settings.GetMessageDestinationUrl
import com.breakneck.domain.usecase.message.SaveSentMessage
import com.breakneck.domain.usecase.message.SendMessageToServer
import com.breakneck.domain.usecase.util.FromTimestampToDateString
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.Locale
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class SMSBroadcastReceiver : BroadcastReceiver(), KoinComponent {

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
        var timestampMillis: Long = 0
        for (message in smsMessages) {
            text.append(message.messageBody + " ")
            timestampMillis = message.timestampMillis
            cellNumber = message.displayOriginatingAddress
        }
        Log.e(TAG, "Message from $cellNumber, body $text")
        val message = Message(
            cellNumber = cellNumber,
            text = text.toString(),
            date = FromTimestampToDateString().execute(timestampMillis, getCurrentLocale(context)),
            sender = Sender.Phone
        )
        saveSentMessage.execute(message)
        sendMessageToServer.execute(url = getMessageDestinationUrl.execute(), message = message)
    }

    private fun getCurrentLocale(context: Context): Locale {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            context.resources.configuration.locales.get(0);
        } else{
            context.resources.configuration.locale;
        }
    }
}