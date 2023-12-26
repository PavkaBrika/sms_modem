package com.breakneck.sms_modem.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log

class NetworkService: Service() {

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

    }

    fun startService() {

    }

    fun stopService() {

    }

}