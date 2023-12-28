package com.breakneck.sms_modem.service

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import android.telephony.SmsManager
import android.util.Log
import com.breakneck.domain.model.ServiceState
import com.breakneck.domain.usecase.GetPort
import com.breakneck.domain.usecase.SaveServiceState
import com.breakneck.sms_modem.app.App
import com.breakneck.sms_modem.presentation.MainActivity
import dagger.android.AndroidInjection
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
import javax.inject.Inject

class NetworkService : Service() {

    //TODO implement dagger instead koin
//    @Inject
//    lateinit var getPort: GetPort
//    @Inject
//    lateinit var saveServiceState: SaveServiceState

    val getPort: GetPort by inject()
    val saveServiceState: SaveServiceState by inject()

    private lateinit var server: NettyApplicationEngine
    private var serviceState: ServiceState = ServiceState.Disabled

    val TAG = "Network service"

    override fun onBind(p0: Intent?): IBinder? {
        Log.e(TAG, "Service bind")
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.e("Network service", "onStartCommand executed with startId: $startId")
        if (intent != null) {
            val extras = intent.extras
            val state = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                extras!!.getSerializable("state", ServiceState::class.java)
            } else {
                extras!!.getSerializable("state") as ServiceState
            }
            Log.e(TAG, "using a intent with state")
            when (state) {
                ServiceState.Disabled -> stopService()
                ServiceState.Enabled -> startService()
                null -> {}
            }
        }
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        Log.e(TAG, "Service create")
        //TODO implement dagger instead koin
//        AndroidInjection.inject(this)

        createServer()

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
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val restartServiceIntent =Intent(applicationContext, NetworkService::class.java).also {
            it.setPackage(packageName)
        }
        val restartServicePendingIntent = PendingIntent.getService(this, 1, restartServiceIntent, PendingIntent.FLAG_IMMUTABLE)
        val alarmService = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmService.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 1000, restartServicePendingIntent)
        super.onTaskRemoved(rootIntent)
    }

    fun startService() {
        if (serviceState is ServiceState.Enabled)
            return
        Log.e(TAG, "Starting service task")
        serviceState = ServiceState.Enabled
        saveServiceState.execute(serviceState)
    }

    fun stopService() {
        Log.e(TAG, "Stopped service task")
        //TODO change deprecated method
        try {
            stopForeground(true)
            stopSelf()
        } catch (e: Exception) {
            Log.e("TAG","Service stopped without being started: ${e.message}")
            e.printStackTrace()
        }

        serviceState = ServiceState.Disabled
        saveServiceState.execute(serviceState)
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
                it.enableVibration(true)
                it.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
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
        Log.e(TAG, "Message sent")
    }


}