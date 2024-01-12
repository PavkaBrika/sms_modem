package com.breakneck.sms_modem.presentation

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.wifi.WifiManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.text.format.Formatter
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.breakneck.domain.model.IpAddress
import com.breakneck.domain.model.MessageDestinationUrl
import com.breakneck.domain.model.MessageFullListVisibilityState
import com.breakneck.domain.model.NetworkState
import com.breakneck.domain.model.Port
import com.breakneck.domain.model.ServiceBoundState
import com.breakneck.domain.model.ServiceIntent
import com.breakneck.domain.model.ServiceState
import com.breakneck.sms_modem.R
import com.breakneck.sms_modem.adapter.MessageAdapter
import com.breakneck.sms_modem.databinding.ActivityMainBinding
import com.breakneck.sms_modem.receiver.NetworkChangeReceiver
import com.breakneck.sms_modem.service.NetworkService
import com.breakneck.sms_modem.service.SERVICE_STATE_RESULT
import com.breakneck.sms_modem.service.SERVICE_TIME_REMAINING_RESULT
import com.breakneck.sms_modem.viewmodel.MainViewModel
//import com.breakneck.sms_modem.viewmodel.MainViewModelFactory
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textfield.TextInputLayout
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.lang.IllegalArgumentException
import java.lang.NullPointerException

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    val TAG = "MainActivity"

    //TODO implement dagger instead koin
//    @Inject
//    lateinit var vmFactory: MainViewModelFactory
//    private lateinit var vm: MainViewModel
//    @Inject
//    lateinit var getPort: GetPort

    private val vm by viewModel<MainViewModel>()

    lateinit var boundNetworkService: NetworkService
    lateinit var networkChangeReceiver: NetworkChangeReceiver

    lateinit var receiver: BroadcastReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        //TODO implement dagger instead koin
//        (applicationContext as App).appComponent.inject(this)
//        vm = ViewModelProvider(this, vmFactory).get(MainViewModel::class.java)

        if (ContextCompat.checkSelfPermission(
                applicationContext,
                android.Manifest.permission.READ_SMS
            ) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                applicationContext,
                android.Manifest.permission.RECEIVE_SMS
            ) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                applicationContext,
                android.Manifest.permission.SEND_SMS
            ) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                applicationContext,
                android.Manifest.permission.ACCESS_NOTIFICATION_POLICY
            ) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                applicationContext,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        android.Manifest.permission.READ_SMS,
                        android.Manifest.permission.RECEIVE_SMS,
                        android.Manifest.permission.SEND_SMS,
                        android.Manifest.permission.ACCESS_NOTIFICATION_POLICY,
                        android.Manifest.permission.POST_NOTIFICATIONS
                    ),
                    10
                )
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        android.Manifest.permission.READ_SMS,
                        android.Manifest.permission.RECEIVE_SMS,
                        android.Manifest.permission.SEND_SMS,
                    ),
                    10
                )
            }
        }

        vm.serverIpAddress.observe(this) { address ->
            binding.ipAddressTextView.text = address.value
        }

        vm.port.observe(this) { port ->
            binding.portTextView.text = getString(R.string.colon_port, port.value)
        }

        binding.activateServiceButton.setOnClickListener {
            try {
                if (vm.serviceRemainingTime.value!! > 0) {
                    serviceAction(vm.networkServiceIntent.value!!)
                    vm.changeServiceIntent()
                    vm.setServiceStateLoading()
                } else {
                    //TODO change location of this message in layout
                    Toast.makeText(this, "Watch ads", Toast.LENGTH_SHORT).show()
                }
            } catch (e: NullPointerException) {
                e.printStackTrace()
                //TODO change hardcode strings to string file
                Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
            }
        }

        binding.settingsButton.setOnClickListener {
            openSettingsBottomSheetDialog()
        }

        binding.watchAdButton.setOnClickListener {
            vm.saveServiceRemainingTime()
            if (vm.networkServiceBoundState.value is ServiceBoundState.Bounded)
                boundNetworkService.updateServiceRemainingTimer()
        }

        binding.messagesRecyclerView.enableClickListener()
        binding.messagesRecyclerView.setOnClickListener {
            vm.changeMessageFullListVisibilityState()
        }
        binding.closeMessageHistoryImageView.setOnClickListener {
            vm.changeMessageFullListVisibilityState()
        }

        vm.networkServiceIntent.observe(this) { intent ->
            when (intent) {
                ServiceIntent.Disable -> {
                    binding.activateServiceButton.text = getString(R.string.disable)
                }

                ServiceIntent.Enable -> {
                    binding.activateServiceButton.text = getString(R.string.enable)
                }
            }
        }

        vm.networkServiceState.observe(this) { state ->
            when (state) {
                ServiceState.Enabled -> {
                    binding.stateTextView.text = getString(R.string.enabled)
                    binding.settingsButton.apply {
                        isEnabled = false
                        setStrokeColorResource(R.color.enabled_button)
                    }
                    binding.activateServiceButton.apply {
                        isEnabled = true
                        setRippleColorResource(R.color.black)
                        setBackgroundColor(
                            ContextCompat.getColor(
                                this@MainActivity,
                                R.color.enabled_button
                            )
                        )
                    }
                    binding.stateCardView.setCardBackgroundColor(
                        ContextCompat.getColor(
                            this,
                            R.color.enabled_card
                        )
                    )
                }

                ServiceState.Disabled -> {
                    binding.stateTextView.text = getString(R.string.disabled)
                    binding.settingsButton.apply {
                        isEnabled = true
                        setStrokeColorResource(R.color.disabled_button)
                        setRippleColorResource(R.color.disabled_button)
                    }
                    binding.activateServiceButton.apply {
                        isEnabled = true
                        setBackgroundColor(
                            ContextCompat.getColor(
                                this@MainActivity,
                                R.color.disabled_button
                            )
                        )
                        setRippleColorResource(R.color.black)
                    }
                    binding.stateCardView.setCardBackgroundColor(
                        ContextCompat.getColor(
                            this,
                            R.color.disabled_card
                        )
                    )
                    try {
                        vm.setDeviceIpAddress(address = IpAddress(value = getDeviceIpAddress()))
                    } catch (e: Exception) {
                        e.printStackTrace()
                        vm.changeNetworkState()
                    }

                }

                ServiceState.Loading -> {
                    //TODO do something animation on loading
                    binding.stateTextView.text = getString(R.string.loading)
                    binding.settingsButton.isEnabled = false
                    binding.activateServiceButton.isEnabled = false
                }
            }
        }

        vm.messageList.observe(this) { list ->
            binding.messagesRecyclerView.apply {
                adapter = MessageAdapter(messagesList = list)
                addItemDecoration(DividerItemDecoration(view.context, DividerItemDecoration.VERTICAL))
            }

        }

        vm.serviceRemainingTime.observe(this) { time ->
            binding.serviceTimeRemainingTextView.text = time.toString()
        }

        vm.messageFullListVisibilityState.observe(this) { state ->
            val constraintSet = ConstraintSet()
            constraintSet.clone(binding.root)
            when (state) {
                MessageFullListVisibilityState.Gone -> {
                    binding.closeMessageHistoryImageView.visibility = View.GONE
                    binding.messagesListCardView.layoutParams.height = (200 * this.resources.displayMetrics.density).toInt()
                    binding.stateCardView.visibility = View.VISIBLE
                    binding.subscriptionCardView.visibility = View.VISIBLE
                    constraintSet.clear(R.id.messagesListCardView, ConstraintSet.BOTTOM)
                }
                MessageFullListVisibilityState.Visible -> {
                    binding.closeMessageHistoryImageView.visibility = View.VISIBLE
                    binding.messagesListCardView.layoutParams.height = 0
                    binding.stateCardView.visibility = View.GONE
                    binding.subscriptionCardView.visibility = View.GONE
                    constraintSet.connect(R.id.messagesListCardView, ConstraintSet.BOTTOM, R.id.rootView, ConstraintSet.BOTTOM, 16)
                }
            }
        }

        vm.networkState.observe(this) { state ->
            when (state) {
                NetworkState.Available -> {

                }
                NetworkState.Unavailable -> {
                    binding.stateTextView.text = getString(R.string.network_connection_unavailable)
                    binding.settingsButton.apply {
                        isEnabled = true
                        setStrokeColorResource(R.color.disabled_button)
                        setRippleColorResource(R.color.disabled_button)
                    }
                    binding.activateServiceButton.apply {
                        isEnabled = false
                        setBackgroundColor(
                            ContextCompat.getColor(
                                this@MainActivity,
                                R.color.disabled_button
                            )
                        )
                        setRippleColorResource(R.color.black)
                    }
                    binding.stateCardView.setCardBackgroundColor(
                        ContextCompat.getColor(
                            this,
                            R.color.disabled_card
                        )
                    )
                }
            }
        }

        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent!!.action.equals(SERVICE_STATE_RESULT))
                    vm.changeServiceIntent()
                else if (intent.action.equals(SERVICE_TIME_REMAINING_RESULT)) {
                    vm.getServiceRemainingTime()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(receiver, IntentFilter(SERVICE_STATE_RESULT))
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(receiver, IntentFilter(SERVICE_TIME_REMAINING_RESULT))

        if ((vm.networkServiceBoundState.value is ServiceBoundState.Unbounded) && (vm.networkServiceState.value is ServiceState.Enabled)) {
            val intent = Intent(this, NetworkService::class.java)
            bindService(intent, networkServiceConnection, 0)
            vm.changeServiceBoundState()
        }
    }

    override fun onStop() {
        super.onStop()
        if (vm.networkServiceBoundState.value is ServiceBoundState.Bounded) {
            unbindService(networkServiceConnection)
            vm.changeServiceBoundState()
        }

        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver)
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver)
    }

    private fun openSettingsBottomSheetDialog() {
        val dialog = BottomSheetDialog(this)
        dialog.setContentView(R.layout.dialog_settings)

        val portTextInputLayout = dialog.findViewById<TextInputLayout>(R.id.portTextInput)
        portTextInputLayout!!.editText!!.setText(vm.port.value!!.value.toString())

        val urlTextInputLayout = dialog.findViewById<TextInputLayout>(R.id.urlTextInput)
        urlTextInputLayout!!.editText!!.setText(vm.messageDestinationUrl.value!!.value)

        dialog.findViewById<Button>(R.id.confirmButton)!!.setOnClickListener {
            val portInputString = portTextInputLayout.editText!!.text.toString()
            if (portInputString.isNotEmpty()) {
                vm.savePort(
                    Port(
                        portInputString.toInt()
                    )
                )
            }
            val urlInputString = urlTextInputLayout.editText!!.text.toString()
            if (urlInputString.isNotEmpty()) {
                vm.saveMessageDestinationUrl(
                    MessageDestinationUrl(
                        //TODO add validation to url
                        urlTextInputLayout.editText!!.text.toString()
                    )
                )
            }
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun getDeviceIpAddress(): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val ipRegex = "\\d+(\\.)\\d+(\\.)\\d+(\\.)\\d+".toRegex()
            val connectivityManager =
                getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val link: LinkProperties =
                connectivityManager.getLinkProperties(connectivityManager.activeNetwork) as LinkProperties
            for (address in link.linkAddresses.indices) {
                if (link.linkAddresses[address].address.hostAddress.matches(ipRegex)) {
                    return link.linkAddresses[address].address.hostAddress
                }
            }
            //TODO MAKE ERROR IN THIS RETURN
            return link.linkAddresses[0].address.hostAddress
        } else {
            val wifiManager =
                applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            return Formatter.formatIpAddress(wifiManager.connectionInfo.ipAddress)
        }
    }

    private fun serviceAction(intent: ServiceIntent) {
        val serviceIntent = Intent(this, NetworkService::class.java)
        serviceIntent.action = intent.toString()
        serviceIntent.putExtra("ipAddress", vm.serverIpAddress.value?.value.toString())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
        try {
            when (intent) {
                ServiceIntent.Disable -> unbindService(networkServiceConnection)
                ServiceIntent.Enable -> bindService(serviceIntent, networkServiceConnection, 0)
            }
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }

    }

    private val networkServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, binder: IBinder?) {
            val networkServiceBinder = binder as NetworkService.NetworkServiceBinder
            boundNetworkService = networkServiceBinder.getService()
            Log.e(TAG, "onNetworkServiceConnected")
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            Log.e(TAG, "onNetworkServiceDisconnected")
        }
    }

    fun RecyclerView.enableClickListener(){
        val gesture = object : GestureDetector.SimpleOnGestureListener(){
            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                this@enableClickListener.performClick()
                return super.onSingleTapConfirmed(e)
            }
        }
        val detector = GestureDetector(this.context, gesture)
        this.setOnTouchListener { v, event -> detector.onTouchEvent(event) }
    }
}