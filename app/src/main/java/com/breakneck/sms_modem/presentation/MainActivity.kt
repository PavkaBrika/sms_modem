package com.breakneck.sms_modem.presentation

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.wifi.WifiManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.format.Formatter
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.breakneck.domain.model.Port
import com.breakneck.domain.model.ServiceState
import com.breakneck.domain.usecase.SavePort
import com.breakneck.sms_modem.R
import com.breakneck.sms_modem.app.App
import com.breakneck.sms_modem.service.NetworkService
import com.breakneck.sms_modem.viewmodel.MainViewModel
import com.breakneck.sms_modem.viewmodel.MainViewModelFactory
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.lang.NullPointerException
import javax.inject.Inject

class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var savePort: SavePort

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        (applicationContext as App).appComponent.inject(this)

        val vm = ViewModelProvider(this, MainViewModelFactory(this)).get(MainViewModel::class.java)

        val ipAddressTextView: TextView = findViewById(R.id.ipAddressTextView)
        ipAddressTextView.text = getDeviceIpAddress()

        val activateButton: Button = findViewById(R.id.activateServiceButton)
        activateButton.setOnClickListener {
            try {
                when (vm.networkServiceState.value!!) {
                    ServiceState.enabled -> {
                        vm.networkServiceState.value = ServiceState.disabled
                    }
                    ServiceState.disabled -> {
                        vm.networkServiceState.value = ServiceState.enabled
                    }
                }
                serviceAction(vm.networkServiceState.value!!)
            } catch (e: NullPointerException) {
                e.printStackTrace()
                Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
            }
        }

        val settingsButton: Button = findViewById(R.id.settingsButton)
        settingsButton.setOnClickListener {
            openSettingsBottomSheetDialog()
        }

        vm.networkServiceState.observe(this) { state ->
            when (state) {
                ServiceState.disabled -> activateButton.text = "Disabled"
                ServiceState.enabled -> activateButton.text = "Enabled"
            }
        }
    }

    private fun openSettingsBottomSheetDialog() {
        val dialog = BottomSheetDialog(this)
        dialog.setContentView(R.layout.dialog_settings)
        dialog.findViewById<Button>(R.id.confirmButton)!!.setOnClickListener {
            savePort.execute(Port(dialog.findViewById<EditText>(R.id.portEditText).toString().toInt()))
            Log.e("TAG", "Port saved")
        }
        dialog.show()
    }

    private fun getDeviceIpAddress(): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            var link: LinkProperties = connectivityManager.getLinkProperties(connectivityManager.activeNetwork) as LinkProperties
            return link.linkAddresses[0].address.hostAddress
        } else {
            val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            return Formatter.formatIpAddress(wifiManager.connectionInfo.ipAddress)
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