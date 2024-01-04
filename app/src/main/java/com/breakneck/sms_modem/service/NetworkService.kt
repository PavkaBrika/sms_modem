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
import android.os.IBinder
import android.os.SystemClock
import android.telephony.SmsManager
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.breakneck.domain.model.Message
import com.breakneck.domain.model.Sender
import com.breakneck.domain.model.ServiceIntent
import com.breakneck.domain.model.ServiceState
import com.breakneck.domain.usecase.GetPort
import com.breakneck.domain.usecase.SaveSentMessage
import com.breakneck.domain.usecase.SaveServiceState
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

const val SERVICE_STATE_RESULT = "com.breakneck.sms_modem.SERVICE_STATE_RESULT"

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

    private lateinit var server: NettyApplicationEngine
    private var serviceState: ServiceState = ServiceState.Disabled

    private lateinit var smsReceiver: SMSBroadcastReceiver
    private lateinit var intentFilter: IntentFilter

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
            val extras = intent.extras
            try {
                val serviceIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    extras!!.getSerializable("intent", ServiceIntent::class.java)
                } else {
                    extras!!.getSerializable("intent") as ServiceIntent
                }
                Log.e(TAG, "using a intent with $serviceIntent")
                when (serviceIntent) {
                    ServiceIntent.Disable -> stopService()
                    ServiceIntent.Enable -> startService()
                    null -> {}
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

        createServer()
        createSmsReceiver()

        val notification: Notification = createNotification()
        startForeground(1, notification)

        serviceState = ServiceState.Enabled
        saveServiceState.execute(serviceState)
        changeServiceStateInActivity()
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            server.stop(1000, 2000)
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
        Log.e(TAG, "Starting service task")
    }

    fun stopService() {
        Log.e(TAG, "Stopped service task")
        try {
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

        serviceState = ServiceState.Disabled
        saveServiceState.execute(serviceState)
        changeServiceStateInActivity()
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
                        call.respondText("Message $message sent to $phone")
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
//                it.enableVibration(true)
//                it.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
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

        val builder: Notification.Builder =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                Notification.Builder(this, notificationChannelId)
            else
                Notification.Builder(this)

        val notification = builder
            .setContentTitle("SMS Service")
            .setContentText("SMS Service is running")
            .setContentIntent(pendingIntent)
            .build()
        return notification
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
        saveSentMessage.execute(Message(cellNumber = phoneNumber, text = messageText.toString(), sender = Sender.Server))
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
            Log.i("TAG", "Receiver name:" + info.activityInfo.name.toString() + "; priority=" + info.priority
            )
        }
    }

    fun changeServiceStateInActivity() {
        Intent(SERVICE_STATE_RESULT)
            .also {
                broadcaster.sendBroadcast(it)
            }
    }

    inner class NetworkServiceBinder : Binder() {

        fun getService(): NetworkService {
            return this@NetworkService
        }
    }

}