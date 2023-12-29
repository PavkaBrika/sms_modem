package com.breakneck.sms_modem.presentation

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.wifi.WifiManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.text.format.Formatter
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.breakneck.domain.model.Port
import com.breakneck.domain.model.ServiceBoundState
import com.breakneck.domain.model.ServiceIntent
import com.breakneck.domain.model.ServiceState
import com.breakneck.sms_modem.R
import com.breakneck.sms_modem.databinding.ActivityMainBinding
import com.breakneck.sms_modem.service.NetworkService
import com.breakneck.sms_modem.service.SERVICE_STATE_RESULT
import com.breakneck.sms_modem.viewmodel.MainViewModel
//import com.breakneck.sms_modem.viewmodel.MainViewModelFactory
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.lang.NullPointerException

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    //TODO implement dagger instead koin
//    @Inject
//    lateinit var vmFactory: MainViewModelFactory
//    private lateinit var vm: MainViewModel
//    @Inject
//    lateinit var getPort: GetPort

    private val vm by viewModel<MainViewModel>()

    lateinit var boundNetworkService: NetworkService

    lateinit var receiver: BroadcastReceiver

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
                vm.changeServiceIntent()
                vm.setServiceStateLoading()
            } catch (e: NullPointerException) {
                e.printStackTrace()
                //TODO change hardcode strings to string file
                Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
            }
        }

        binding.settingsButton.setOnClickListener {
            openSettingsBottomSheetDialog()
        }

        vm.networkServiceIntent.observe(this) { intent ->
            lifecycleScope.launch(Dispatchers.Default) {
                serviceAction(intent)
                //TODO change hardcode strings to string file
                when (intent) {
                    ServiceIntent.Disable -> {
                        binding.activateServiceButton.text = "Enable"
                    }

                    ServiceIntent.Enable -> {
                        binding.activateServiceButton.text = "Disable"
                    }
                }
            }
        }

        vm.networkServiceState.observe(this) { state ->
            when (state) {
                ServiceState.Enabled -> {
                    binding.stateTextView.text = "Enabled"
                }

                ServiceState.Disabled -> {
                    binding.stateTextView.text = "Disabled"
                }

                ServiceState.Loading -> {
                    //TODO do something animation on loading
                    binding.stateTextView.text = "Loading"
                }
            }
        }

        receiver = object : BroadcastReceiver() {
            override fun onReceive(p0: Context?, p1: Intent?) {
                vm.getServiceState()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(receiver, IntentFilter(SERVICE_STATE_RESULT))
    }

    override fun onStop() {
        super.onStop()
        if (vm.networkServiceBoundState.value is ServiceBoundState.Bounded)
            unbindService(networkServiceConnection)
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver)
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
            return link.linkAddresses[1].address.hostAddress
        } else {
            val wifiManager =
                applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            return Formatter.formatIpAddress(wifiManager.connectionInfo.ipAddress)
        }
    }

    private fun serviceAction(intent: ServiceIntent) {
        val serviceIntent = Intent(this, NetworkService::class.java)
        serviceIntent.putExtra("intent", intent)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
        if (intent is ServiceIntent.Enable)
            bindService(serviceIntent, networkServiceConnection, Context.BIND_AUTO_CREATE)
        else
            unbindService(networkServiceConnection)
    }

    private val networkServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, binder: IBinder?) {
            val networkServiceBinder = binder as NetworkService.NetworkServiceBinder
            boundNetworkService = networkServiceBinder.getService()
//            vm.changeServiceBoundState()
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
//            vm.changeServiceBoundState()
        }
    }
}