package com.breakneck.sms_modem.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.breakneck.domain.usecase.GetMessageDestinationUrl
import com.breakneck.domain.usecase.SendMessageToServer
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

    val sendMessageToServer: SendMessageToServer by inject()
    val getMessageDestinationUrl: GetMessageDestinationUrl by inject()


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


    override fun onReceive(p0: Context?, p1: Intent?) {

    }
}