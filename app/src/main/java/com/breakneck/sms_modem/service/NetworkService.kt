package com.breakneck.sms_modem.service

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
import android.util.Log
import com.breakneck.sms_modem.presentation.MainActivity

class NetworkService : Service() {

    val TAG = "Network service"

    override fun onBind(p0: Intent?): IBinder? {
        Log.e(TAG, "Service bind")
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.e("Network service", "onStartCommand executed with startId: $startId")
        if (intent != null) {
            val action = intent.action
            Log.e(TAG, "using a intent with action: $action")
            when (action) {
                "enable" -> startService()
                "disable" -> stopService()
            }
        }
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        Log.e(TAG, "Service create")

        createServer()

        val notification: Notification = createNotification()
        startForeground(1, notification)
    }

    fun startService() {

    }

    fun stopService() {

    }

    fun createServer() {

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


}