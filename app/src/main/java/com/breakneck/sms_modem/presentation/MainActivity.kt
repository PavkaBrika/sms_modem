package com.breakneck.sms_modem.presentation

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.breakneck.domain.util.ServiceState
import com.breakneck.sms_modem.R
import com.breakneck.sms_modem.service.NetworkService
import com.breakneck.sms_modem.viewmodel.MainViewModel
import com.breakneck.sms_modem.viewmodel.MainViewModelFactory
import java.lang.NullPointerException

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val vm = ViewModelProvider(this, MainViewModelFactory(this)).get(MainViewModel::class.java)

        val activateButton: Button = findViewById(R.id.activateServiceButton)
        activateButton.setOnClickListener {
            try {
                when (vm.networkServiceState.value!!) {
                    ServiceState.enabled -> {
                        activateButton.text = "Disabled"
                    }
                    ServiceState.disabled -> {
                        activateButton.text = "Enabled"
                    }
                }
                serviceAction(vm.networkServiceState.value!!)
            } catch (e: NullPointerException) {
                e.printStackTrace()
                Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
            }

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