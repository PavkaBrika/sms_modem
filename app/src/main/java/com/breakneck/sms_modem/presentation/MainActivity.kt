package com.breakneck.sms_modem.presentation

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.lifecycle.ViewModelProvider
import com.breakneck.domain.util.ServiceState
import com.breakneck.sms_modem.R
import com.breakneck.sms_modem.service.NetworkService

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        val vm = ViewModelProvider(this,)

        val activateButton: Button = findViewById(R.id.activateServiceButton)
        activateButton.setOnClickListener {

        }
    }

    private fun serviceAction(action: ServiceState) {
        Intent(this, NetworkService::class.java)
            .also {
                 it.action = when (action) {
                     ServiceState.enabled -> {
                         "disable"
                     }
                     ServiceState.disabled -> {
                         "enable"
                     }
                 }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(it)
                } else {
                    startService(it)
                }
            }
    }
}