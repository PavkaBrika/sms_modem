package com.breakneck.sms_modem.service

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ResolveInfo
import android.graphics.Color
import android.os.Binder
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import android.os.SystemClock
import android.telephony.SmsManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.breakneck.domain.model.Message
import com.breakneck.domain.model.Sender
import com.breakneck.domain.model.ServiceIntent
import com.breakneck.domain.model.ServiceState
import com.breakneck.domain.usecase.settings.GetPort
import com.breakneck.domain.usecase.message.SaveSentMessage
import com.breakneck.domain.usecase.service.GetServiceRemainingTime
import com.breakneck.domain.usecase.service.SaveServiceRemainingTime
import com.breakneck.domain.usecase.service.SaveServiceState
import com.breakneck.domain.usecase.util.FromTimestampToDateString
import com.breakneck.sms_modem.R
import com.breakneck.sms_modem.presentation.MainActivity
import com.breakneck.sms_modem.receiver.SMSBroadcastReceiver
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.gson.gson
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine
import org.koin.android.ext.android.inject
import java.lang.StringBuilder
import java.util.Locale

const val SERVICE_STATE_RESULT = "com.breakneck.sms_modem.SERVICE_STATE_RESULT"
const val SERVICE_TIME_REMAINING_RESULT = "com.breakneck.sms_modem.SERVICE_TIME_REMAINING_RESULT"

open class NetworkService : Service() {

    //TODO implement dagger instead koin
//    @Inject
//    lateinit var getPort: GetPort
//    @Inject
//    lateinit var saveServiceState: SaveServiceState

    private val binder: IBinder = NetworkServiceBinder()
    private lateinit var broadcaster: LocalBroadcastManager

    val getPort: GetPort by inject()
    val saveServiceState: SaveServiceState by inject()
    val saveSentMessage: SaveSentMessage by inject()
    val getServiceRemainingTime: GetServiceRemainingTime by inject()
    val saveServiceRemainingTime: SaveServiceRemainingTime by inject()

    private lateinit var server: NettyApplicationEngine
    private var serviceState: ServiceState = ServiceState.Disabled

    private lateinit var smsReceiver: SMSBroadcastReceiver
    private lateinit var intentFilter: IntentFilter
    private lateinit var timer: CountDownTimer
    private var ipAddress: String? = null

    val TAG = "NetworkService"

    override fun onBind(intent: Intent?): IBinder? {
        Log.e(TAG, "Service bind")
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.e(TAG, "Service unbind")
        return super.onUnbind(intent)
    }

    override fun onRebind(intent: Intent?) {
        Log.e(TAG, "Service rebind")
        super.onRebind(intent)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.e(TAG, "onStartCommand executed with startId: $startId")
        if (intent != null) {
            val action = intent.action
            ipAddress = intent.extras?.getString("ipAddress").toString()
            try {
                Log.e(TAG, "using a intent with $action")
                when (action) {
                    ServiceIntent.Disable.toString() -> stopService()
                    ServiceIntent.Enable.toString() -> startService()
                }
            } catch (e: NullPointerException) {
                e.printStackTrace()
            }

        }
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        Log.e(TAG, "Service create")
        //TODO implement dagger instead koin
//        AndroidInjection.inject(this)
        broadcaster = LocalBroadcastManager.getInstance(this)

        val notification: Notification = createNotification()
        startForeground(1, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            server.stop(1000, 2000)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            unregisterReceiver(smsReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        Log.e(TAG, "Service destroyed")
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val restartServiceIntent = Intent(applicationContext, NetworkService::class.java).also {
            it.setPackage(packageName)
        }
        val restartServicePendingIntent =
            PendingIntent.getService(this, 1, restartServiceIntent, PendingIntent.FLAG_IMMUTABLE)
        val alarmService =
            applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmService.set(
            AlarmManager.ELAPSED_REALTIME,
            SystemClock.elapsedRealtime() + 1000,
            restartServicePendingIntent
        )
        super.onTaskRemoved(rootIntent)
    }

    fun startService() {
        if (serviceState is ServiceState.Enabled)
            return

        createServer()
        createSmsReceiver()

        serviceState = ServiceState.Enabled
        saveServiceState.execute(serviceState)
        changeServiceStateInActivity()
        updateServiceRemainingTimer()

        Log.e(TAG, "Starting service task")
    }

    fun stopService() {
        Log.e(TAG, "Stopped service task")
        try {
            timer.cancel()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            serviceState = ServiceState.Disabled
            saveServiceState.execute(serviceState)
            changeServiceStateInActivity()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(STOP_FOREGROUND_REMOVE)
            } else {
                stopForeground(true)
            }
            stopSelf()
        } catch (e: Exception) {
            Log.e("TAG", "Service stopped without being started: ${e.message}")
            e.printStackTrace()
        }
    }

    fun createServer() {
        server = embeddedServer(Netty, port = getPort.execute().value) {
            install(ContentNegotiation) {
                gson()
            }
            routing {
                get("/") {
                    call.respondText("SERVICE ENABLED")
                }
                get("/{phone}/{message}") {
                    val phone = call.parameters["phone"]
                    val message = call.parameters["message"]
                    if ((phone != "") && (message != "")) {
                        sendSMS(phoneNumber = phone!!, message = message!!)
                        val date = FromTimestampToDateString().execute(
                            System.currentTimeMillis() / 1000,
                            getCurrentLocale(applicationContext)
                        )
                        call.respondText("Date $date,Message $message sent to $phone")
                    } else {
                        call.respondText("Error")
                    }
                }
            }
        }.start(wait = false)
        Log.e(TAG, "Server created")
    }

    fun createNotification(): Notification {
        val notificationChannelId = "SMS_SERVICE_CHANNEL"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                notificationChannelId,
                "SMS service",
                NotificationManager.IMPORTANCE_DEFAULT
            ).let {
                it.description = "SMS Service channel"
                it.enableLights(true)
                it.lightColor = Color.RED
                it
            }
            notificationManager.createNotificationChannel(channel)
        }

        val pendingIntent: PendingIntent =
            Intent(this, MainActivity::class.java)
                .let { notificationIntent ->
                    PendingIntent.getActivity(
                        this,
                        0,
                        notificationIntent,
                        PendingIntent.FLAG_MUTABLE
                    )
                }

        return NotificationCompat.Builder(this, notificationChannelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("SMS Service")
            .setContentText(
                if (ipAddress != null) {
                    "SMS Service is running with ip $ipAddress:${getPort.execute().value}"
                } else {
                    "SMS Service is running"
                }
            )
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun sendSMS(phoneNumber: String, message: String) {
        val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            applicationContext.getSystemService(SmsManager::class.java)
        } else {
            SmsManager.getDefault()
        }
        val messageParts = smsManager.divideMessage(message)
        smsManager.sendMultipartTextMessage(phoneNumber, null, messageParts, null, null)
        val messageText = StringBuilder("")
        for (text in messageParts) {
            messageText.append("$text ")
        }
        saveSentMessage.execute(
            Message(
                cellNumber = phoneNumber,
                text = messageText.toString(),
                date = FromTimestampToDateString().execute(
                    System.currentTimeMillis() / 1000,
                    getCurrentLocale(applicationContext)
                ),
                sender = Sender.Server
            )
        )
        Log.e(TAG, "Message sent")
    }

    private fun createSmsReceiver() {
        smsReceiver = SMSBroadcastReceiver()
        intentFilter = IntentFilter().also {
            it.addAction("android.provider.Telephony.SMS_RECEIVED")
            it.priority = 2147483647
        }
        registerReceiver(smsReceiver, intentFilter)

        val intent = Intent("android.provider.Telephony.SMS_RECEIVED")
        val infos: List<ResolveInfo> = packageManager.queryBroadcastReceivers(intent, 0)
        for (info in infos) {
            Log.i(
                "TAG",
                "Receiver name:" + info.activityInfo.name.toString() + "; priority=" + info.priority
            )
        }
    }

    fun changeServiceStateInActivity() {
        Intent(SERVICE_STATE_RESULT)
            .also {
                broadcaster.sendBroadcast(it)
            }
    }

    fun updateServiceTimeRemainingInActivity() {
        Intent(SERVICE_TIME_REMAINING_RESULT)
            .also {
                broadcaster.sendBroadcast(it)
            }
    }

    fun updateServiceRemainingTimer() {
        try {
            timer.cancel()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        timer = object : CountDownTimer(getServiceRemainingTime.execute(), 1000) {

            override fun onTick(millisUntilFinished: Long) {
//                Log.e(TAG, "CountDownTimer second remaining until finished = ${millisUntilFinished / 1000}")
                updateServiceTimeRemainingInActivity()
                saveServiceRemainingTime.execute(millisUntilFinished)
            }

            override fun onFinish() {
                Log.e(TAG, "CountDownTimerFinished")
                saveServiceRemainingTime.execute(0)
                stopService()
            }
        }.start()
    }

    private fun getCurrentLocale(context: Context): Locale {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.resources.configuration.locales.get(0);
        } else {
            context.resources.configuration.locale;
        }
    }

    inner class NetworkServiceBinder : Binder() {

        fun getService(): NetworkService {
            return this@NetworkService
        }
    }

}