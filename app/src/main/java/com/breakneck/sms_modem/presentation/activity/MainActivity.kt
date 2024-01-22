package com.breakneck.sms_modem.presentation.activity

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
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.appodeal.ads.Appodeal
import com.appodeal.ads.BannerCallbacks
import com.breakneck.domain.model.FragmentTag
import com.breakneck.domain.model.MessageDestinationUrl
import com.breakneck.domain.model.Port
import com.breakneck.domain.model.ServiceBoundState
import com.breakneck.domain.model.ServiceIntent
import com.breakneck.domain.model.ServiceState
import com.breakneck.domain.usecase.settings.SaveRemindNotificationTime
import com.breakneck.sms_modem.R
import com.breakneck.sms_modem.databinding.ActivityMainBinding
import com.breakneck.sms_modem.presentation.fragment.InfoFragment
import com.breakneck.sms_modem.presentation.fragment.MainFragment
import com.breakneck.sms_modem.presentation.fragment.MessagesFragment
import com.breakneck.sms_modem.receiver.RECEIVER_NEW_MESSAGE
import com.breakneck.sms_modem.service.ERROR
import com.breakneck.sms_modem.service.NetworkService
import com.breakneck.sms_modem.service.SERVICE_ERROR
import com.breakneck.sms_modem.service.SERVICE_NEW_MESSAGE
import com.breakneck.sms_modem.service.SERVICE_START_SUCCESS
import com.breakneck.sms_modem.service.SERVICE_STATE_RESULT
import com.breakneck.sms_modem.service.SERVICE_TIME_REMAINING_RESULT
import com.breakneck.sms_modem.service.SERVICE_UPDATE_ADS
import com.breakneck.sms_modem.viewmodel.InfoFragmentViewModel
import com.breakneck.sms_modem.viewmodel.MainActivityViewModel
import com.breakneck.sms_modem.viewmodel.MessageFragmentViewModel
//import com.breakneck.sms_modem.viewmodel.MainViewModelFactory
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textfield.TextInputLayout
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.lang.IllegalArgumentException

class MainActivity : AppCompatActivity(), MainFragment.ActivityInterface {

    private lateinit var binding: ActivityMainBinding

    val TAG = "MainActivity"

    //TODO implement dagger instead koin
//    @Inject
//    lateinit var vmFactory: MainViewModelFactory
//    private lateinit var vm: MainViewModel
//    @Inject
//    lateinit var getPort: GetPort

    private val vm by viewModel<MainActivityViewModel>()
    private val messageFragmentViewModel by viewModel<MessageFragmentViewModel>()

    lateinit var boundNetworkService: NetworkService
//    lateinit var networkChangeReceiver: NetworkChangeReceiver

    lateinit var receiver: BroadcastReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        Appodeal.setBannerViewId(R.id.bannerView)
        Appodeal.show(this, Appodeal.BANNER_VIEW)
        Appodeal.set728x90Banners(true)
        Appodeal.setLogLevel(com.appodeal.ads.utils.Log.LogLevel.debug)
        Appodeal.setChildDirectedTreatment(false)
        Appodeal.muteVideosIfCallsMuted(true)
        Appodeal.setBannerCallbacks(object: BannerCallbacks {
            override fun onBannerClicked() {
                Log.e("Appodeal", "Banner clicked")
            }

            override fun onBannerExpired() {
                Log.e("Appodeal", "Banner expired")
            }

            override fun onBannerFailedToLoad() {
                Log.e("Appodeal", "Banner failed to load")
            }

            override fun onBannerLoaded(height: Int, isPrecache: Boolean) {
                Log.e("Appodeal", "Banner loaded")
            }

            override fun onBannerShowFailed() {
                Log.e("Appodeal", "Banner show failed")
            }

            override fun onBannerShown() {
                Log.e("Appodeal", "Banner shown")
            }
        })

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

        val toolbar = supportActionBar

        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.item_main -> {
                    replaceFragment(FragmentTag.Main)
                    toolbar!!.title = resources.getString(R.string.main)
                    true
                }
                R.id.item_list -> {
                    replaceFragment(FragmentTag.Message)
                    toolbar!!.title = resources.getString(R.string.messages)
                    true
                }
                R.id.item_info -> {
                    replaceFragment(FragmentTag.Info)
                    toolbar!!.title = resources.getString(R.string.info)
                    true
                }
                else -> false
            }
        }

        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent!!.action.equals(SERVICE_STATE_RESULT))
                    vm.changeServiceIntent()
                else if (intent.action.equals(SERVICE_TIME_REMAINING_RESULT)) {
                    vm.getServiceRemainingTime()
                } else if (intent.action.equals(SERVICE_NEW_MESSAGE)) {
                    messageFragmentViewModel.getAllMessages()
                } else if (intent.action.equals(SERVICE_ERROR)) {
                    vm.setServiceError(intent.extras!!.getString(ERROR, ""))
                } else if (intent.action.equals(SERVICE_START_SUCCESS)) {
                    vm.setServiceError("")
                } else if (intent.action.equals(RECEIVER_NEW_MESSAGE)) {
                    messageFragmentViewModel.getAllMessages()
                } else if (intent.action.equals(SERVICE_UPDATE_ADS)) {
                    vm.getRemainingAds()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        LocalBroadcastManager.getInstance(this).apply {
            registerReceiver(receiver, IntentFilter(SERVICE_STATE_RESULT))
            registerReceiver(receiver, IntentFilter(SERVICE_TIME_REMAINING_RESULT))
            registerReceiver(receiver, IntentFilter(SERVICE_NEW_MESSAGE))
            registerReceiver(receiver, IntentFilter(RECEIVER_NEW_MESSAGE))
            registerReceiver(receiver, IntentFilter(SERVICE_ERROR))
            registerReceiver(receiver, IntentFilter(SERVICE_START_SUCCESS))
            registerReceiver(receiver, IntentFilter(SERVICE_UPDATE_ADS))
        }

        if ((vm.networkServiceBoundState.value is ServiceBoundState.Unbounded) && (vm.networkServiceState.value is ServiceState.Enabled)) {
            val intent = Intent(this, NetworkService::class.java)
            bindService(intent, networkServiceConnection, 0)
            vm.onServiceBind()
        }
    }

    override fun onStop() {
        super.onStop()
        if (vm.networkServiceBoundState.value is ServiceBoundState.Bounded) {
            unbindService(networkServiceConnection)
            vm.onServiceUnbind()
        }

        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver)
    }

    private fun replaceFragment(fragmentTag: FragmentTag) {
        val fragmentManager = supportFragmentManager
        val transaction = fragmentManager.beginTransaction()
        val currentFragment = fragmentManager.findFragmentById(R.id.frameLayout)
        var nextFragment = fragmentManager.findFragmentByTag(fragmentTag.toString())

        try {
            if (currentFragment != null)
                transaction.detach(currentFragment)
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }
        if (nextFragment == null) {
            nextFragment = createFragment(fragmentTag)
            transaction.add(R.id.frameLayout, nextFragment, fragmentTag.toString())
        } else {
            transaction.attach(nextFragment)
        }
        transaction.commit()
    }

    private fun createFragment(tag: FragmentTag): Fragment {
        return when (tag) {
            FragmentTag.Main -> MainFragment()
            FragmentTag.Message -> MessagesFragment()
            FragmentTag.Info -> InfoFragment()
        }
    }

    override fun showSettingsBottomSheetDialog() {
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
                if ((urlInputString.startsWith("http://")) || (urlInputString.startsWith("https://"))) {
                    vm.saveMessageDestinationUrl(
                        MessageDestinationUrl(
                            urlInputString
                        )
                    )
                    urlTextInputLayout.error = null
                    dialog.dismiss()
                }
                else
                    urlTextInputLayout.error = getString(R.string.url_must_begin_with_http_or_https)
            } else {
                dialog.dismiss()
            }
        }

        dialog.findViewById<Button>(R.id.cancelButton)!!.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    override fun getDeviceIpAddress(): String {
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

    override fun serviceAction(intent: ServiceIntent) {
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
                ServiceIntent.Disable -> {
                    unbindService(networkServiceConnection)
                }
                ServiceIntent.Enable -> {
                    bindService(serviceIntent, networkServiceConnection, 0)
                }
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
            vm.onServiceBind()
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            Log.e(TAG, "onNetworkServiceDisconnected")
            vm.onServiceUnbind()
        }
    }

    override fun updateServiceRemainingTimer() {
        if (::boundNetworkService.isInitialized)
            boundNetworkService.updateServiceRemainingTimer()
    }
}