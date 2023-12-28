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
import com.breakneck.domain.usecase.GetPort
import com.breakneck.sms_modem.R
import com.breakneck.sms_modem.app.App
import com.breakneck.sms_modem.databinding.ActivityMainBinding
import com.breakneck.sms_modem.service.NetworkService
import com.breakneck.sms_modem.viewmodel.MainViewModel
import com.breakneck.sms_modem.viewmodel.MainViewModelFactory
import com.google.android.material.bottomsheet.BottomSheetDialog
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.lang.NullPointerException
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    //TODO implement dagger instead koin
//    @Inject
//    lateinit var vmFactory: MainViewModelFactory
//    private lateinit var vm: MainViewModel
//    @Inject
//    lateinit var getPort: GetPort

    private val vm by viewModel<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        //TODO implement dagger instead koin
//        (applicationContext as App).appComponent.inject(this)
//        vm = ViewModelProvider(this, vmFactory).get(MainViewModel::class.java)

        binding.ipAddressTextView.text = getDeviceIpAddress()

        vm.port.observe(this) { port ->
            binding.portTextView.text = getString(R.string.colon_port, port.value)
        }

        binding.activateServiceButton.setOnClickListener {
            try {
                vm.changeServiceState()
            } catch (e: NullPointerException) {
                e.printStackTrace()
                //TODO change hardcode strings to string file
                Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
            }
        }

        binding.settingsButton.setOnClickListener {
            openSettingsBottomSheetDialog()
        }

        vm.networkServiceState.observe(this) { state ->
            serviceAction(state)
            //TODO change hardcode strings to string file
            when (state) {
                ServiceState.Disabled -> binding.activateServiceButton.text = "Enable"
                ServiceState.Enabled -> binding.activateServiceButton.text = "Enabled"
            }
        }
    }

    private fun openSettingsBottomSheetDialog() {
        val dialog = BottomSheetDialog(this)
        dialog.setContentView(R.layout.dialog_settings)
        dialog.findViewById<Button>(R.id.confirmButton)!!.setOnClickListener {
            vm.savePort(
                Port(
                    dialog.findViewById<EditText>(R.id.portEditText)!!.text.toString().toInt()
                )
            )
            Log.e("TAG", "Port saved")
        }
        dialog.show()
    }

    private fun getDeviceIpAddress(): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val connectivityManager =
                getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val link: LinkProperties =
                connectivityManager.getLinkProperties(connectivityManager.activeNetwork) as LinkProperties
            //TODO add some validation in ip
            return link.linkAddresses[0].address.hostAddress
        } else {
            val wifiManager =
                applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            return Formatter.formatIpAddress(wifiManager.connectionInfo.ipAddress)
        }
    }

    private fun serviceAction(action: ServiceState) {
        Intent(this, NetworkService::class.java)
            .also {
                it.putExtra("state", action)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(it)
                } else {
                    startService(it)
                }
            }
    }
}