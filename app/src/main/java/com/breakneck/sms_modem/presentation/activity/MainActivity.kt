package com.breakneck.sms_modem.presentation.activity

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.text.format.Formatter
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.appodeal.ads.Appodeal
import com.appodeal.ads.BannerCallbacks
import com.breakneck.domain.model.FragmentTag
import com.breakneck.domain.model.MessageDestinationUrl
import com.breakneck.domain.model.Port
import com.breakneck.domain.model.ServiceBoundState
import com.breakneck.domain.model.ServiceIntent
import com.breakneck.domain.model.ServiceState
import com.breakneck.domain.model.SubscriptionPlan
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
import com.breakneck.sms_modem.viewmodel.MainActivityViewModel
import com.breakneck.sms_modem.viewmodel.MessageFragmentViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Locale


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

    lateinit var receiver: BroadcastReceiver

    val permissionList: MutableList<String> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        /**
         * UNCOMMENT FOR LANGUAGE TEST
         */
//        val locale = Locale("en")
//        Locale.setDefault(locale)
//        val config = Configuration()
//        config.locale = locale
//        resources.updateConfiguration(config, resources.displayMetrics)
        /**
         * UNCOMMENT FOR LANGUAGE TEST
         */
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
        checkPermissions()

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

        if (vm.isSubscriptionDialogOpened.value!!)
            showSubscriptionPlansDialog()
        if (vm.isHowToUseDialogOpened.value!!)
            showHowToUseDialog()

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

        val connectivityManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getSystemService(ConnectivityManager::class.java)
        } else {
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        }

        val networkRequestBuilder = NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                networkRequestBuilder.addTransportType(NetworkCapabilities.TRANSPORT_WIFI_AWARE)

        val networkRequest = networkRequestBuilder.build()

        connectivityManager.registerNetworkCallback(networkRequest, object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)

                lifecycleScope.launch(Dispatchers.Main) {
                    vm.onNetworkAvailable()
                }
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                super.onCapabilitiesChanged(network, networkCapabilities)

                lifecycleScope.launch(Dispatchers.Main) {
                    vm.onNetworkAvailable()
                }
            }

//            override fun onLosing(network: Network, maxMsToLive: Int) {
//                super.onLosing(network, maxMsToLive)
//                lifecycleScope.launch(Dispatchers.Main) {
//                    vm.onNetworkUnavailable()
//                }
//            }

            override fun onLost(network: Network) {
                super.onLost(network)
                lifecycleScope.launch(Dispatchers.Main) {
                    vm.onNetworkUnavailable()
                }
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main_toolbar, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.howToUseMenuItem -> showHowToUseDialog()
        }
        return super.onOptionsItemSelected(item)
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

    private fun showAppFunctionalityInfoDialog() {
        val dialog = BottomSheetDialog(this)
        dialog.setContentView(R.layout.dialog_app_functions_info)
        dialog.setCancelable(false)

        if (ContextCompat.checkSelfPermission(
                applicationContext,
                android.Manifest.permission.READ_SMS
            ) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(android.Manifest.permission.READ_SMS)
        }
        if (ContextCompat.checkSelfPermission(
            applicationContext,
            android.Manifest.permission.RECEIVE_SMS
            ) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(android.Manifest.permission.RECEIVE_SMS)
        }
        if (ContextCompat.checkSelfPermission(
                applicationContext,
                android.Manifest.permission.SEND_SMS
            ) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(android.Manifest.permission.SEND_SMS)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(
                    applicationContext,
                    android.Manifest.permission.ACCESS_NOTIFICATION_POLICY
                ) != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(android.Manifest.permission.ACCESS_NOTIFICATION_POLICY)
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    applicationContext,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        dialog.findViewById<Button>(R.id.confirmButton)!!.setOnClickListener {
            ActivityCompat.requestPermissions(
                this,
                permissionList.toTypedArray(),
                10
            )
            dialog.dismiss()
        }

        dialog.findViewById<Button>(R.id.cancelButton)!!.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
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

    override fun showSubscriptionPlansDialog() {
        val dialog = BottomSheetDialog(this)
        dialog.setContentView(R.layout.dialog_subscription_plans)
        dialog.setCancelable(false)

        val annualCardView = dialog.findViewById<MaterialCardView>(R.id.annualSubscriptionCardView)
        val annualSubscriptionTitleTextView = dialog.findViewById<TextView>(R.id.annualSubscriptionTitleTextView)
        val annualSubscriptionPriceTextView = dialog.findViewById<TextView>(R.id.annualSubscriptionPriceTextView)

        val seasonalCardView = dialog.findViewById<MaterialCardView>(R.id.threeMonthsSubscriptionCardView)
        val seasonalSubscriptionTitleTextView = dialog.findViewById<TextView>(R.id.seasonalSubscriptionTitleTextView)
        val seasonalSubscriptionPriceTextView = dialog.findViewById<TextView>(R.id.seasonalSubscriptionPriceTextView)

        val monthCardView = dialog.findViewById<MaterialCardView>(R.id.monthlySubscriptionCardView)
        val monthSubscriptionTitleTextView = dialog.findViewById<TextView>(R.id.monthlySubscriptionTitleTextView)
        val monthSubscriptionPriceTextView = dialog.findViewById<TextView>(R.id.monthlySubscriptionPriceTextView)

        val confirmButton = dialog.findViewById<Button>(R.id.confirmButton)

        vm.selectedSubscription.observe(dialog) { plan ->
            when (plan) {
                SubscriptionPlan.MONTHLY -> {
                    monthCardView!!.strokeColor = ContextCompat.getColor(this, R.color.disabled_card)
                    monthCardView.strokeWidth = 10
                    monthSubscriptionPriceTextView!!.setTextColor(ContextCompat.getColor(this, R.color.disabled_card))
                    monthSubscriptionTitleTextView!!.setTextColor(ContextCompat.getColor(this, R.color.disabled_card))

                    seasonalCardView!!.strokeColor = Color.BLACK
                    seasonalCardView.strokeWidth = 5
                    seasonalSubscriptionPriceTextView!!.setTextColor(Color.BLACK)
                    seasonalSubscriptionTitleTextView!!.setTextColor(Color.BLACK)

                    annualCardView!!.strokeColor = Color.BLACK
                    annualCardView.strokeWidth = 5
                    annualSubscriptionTitleTextView!!.setTextColor(Color.BLACK)
                    annualSubscriptionPriceTextView!!.setTextColor(Color.BLACK)

                    //TODO CHANGE STRING
                    confirmButton!!.text = "${getString(R.string.subscribe_now)}\n150.00 Р per month"
                }
                SubscriptionPlan.SEASONALLY -> {
                    monthCardView!!.strokeColor = Color.BLACK
                    monthCardView.strokeWidth = 5
                    monthSubscriptionPriceTextView!!.setTextColor(Color.BLACK)
                    monthSubscriptionTitleTextView!!.setTextColor(Color.BLACK)

                    seasonalCardView!!.strokeColor = ContextCompat.getColor(this, R.color.disabled_card)
                    seasonalCardView.strokeWidth = 10
                    seasonalSubscriptionPriceTextView!!.setTextColor(ContextCompat.getColor(this, R.color.disabled_card))
                    seasonalSubscriptionTitleTextView!!.setTextColor(ContextCompat.getColor(this, R.color.disabled_card))

                    annualCardView!!.strokeColor = Color.BLACK
                    annualCardView.strokeWidth = 5
                    annualSubscriptionTitleTextView!!.setTextColor(Color.BLACK)
                    annualSubscriptionPriceTextView!!.setTextColor(Color.BLACK)

                    confirmButton!!.text = "${getString(R.string.subscribe_now)}\n387.00 Р per 3 months"
                }
                SubscriptionPlan.ANNUALLY -> {
                    monthCardView!!.strokeColor = Color.BLACK
                    monthCardView.strokeWidth = 5
                    monthSubscriptionPriceTextView!!.setTextColor(Color.BLACK)
                    monthSubscriptionTitleTextView!!.setTextColor(Color.BLACK)

                    seasonalCardView!!.strokeColor = Color.BLACK
                    seasonalCardView.strokeWidth = 5
                    seasonalSubscriptionPriceTextView!!.setTextColor(Color.BLACK)
                    seasonalSubscriptionTitleTextView!!.setTextColor(Color.BLACK)

                    annualCardView!!.strokeColor = ContextCompat.getColor(this, R.color.disabled_card)
                    annualCardView.strokeWidth = 10
                    annualSubscriptionTitleTextView!!.setTextColor(ContextCompat.getColor(this, R.color.disabled_card))
                    annualSubscriptionPriceTextView!!.setTextColor(ContextCompat.getColor(this, R.color.disabled_card))

                    confirmButton!!.text = "${getString(R.string.subscribe_now)}\n1188.00 Р per year"
                }
            }
        }

        annualCardView!!.setOnClickListener { vm.onAnnualSubscriptionClicked() }
        seasonalCardView!!.setOnClickListener { vm.onSeasonSubscriptionClicked() }
        monthCardView!!.setOnClickListener { vm.onMonthSubscriptionClicked() }

        dialog.findViewById<ImageView>(R.id.cancelImageView)!!.setOnClickListener {
            dialog.dismiss()
        }

        dialog.findViewById<TextView>(R.id.termsTextView)!!.setOnClickListener {
            showSubscriptionsTermsDialog()
        }

        dialog.findViewById<TextView>(R.id.privacyPolicyTextView)!!.setOnClickListener {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://smsmodem-privacy-policy.ucoz.net/")
                )
            )
        }

        dialog.setOnDismissListener {
            vm.onSubscriptionDialogClose()
        }

        dialog.show()
        vm.onSubscriptionDialogOpen()
    }

    fun showSubscriptionsTermsDialog() {
        val dialog = BottomSheetDialog(this)
        dialog.setContentView(R.layout.dialog_subscription_terms)

        dialog.findViewById<Button>(R.id.cancelButton)!!.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showHowToUseDialog() {
        val dialog = BottomSheetDialog(this)
        dialog.setContentView(R.layout.dialog_how_to_use)

        if ((vm.serverIpAddress.value != null) && (vm.serverIpAddress.value!!.value != "")) {
            dialog.findViewById<TextView>(R.id.requestToSendTextView)!!.text = getString(
                R.string.send_message_request,
                vm.serverIpAddress.value!!.value,
                vm.port.value!!.value.toString()
            )
        } else {
            dialog.findViewById<TextView>(R.id.requestToSendTextView)!!.visibility = View.GONE
            dialog.findViewById<TextView>(R.id.requestToSendHintTextView)!!.visibility = View.GONE
        }

        if ((vm.messageDestinationUrl.value != null) && (vm.messageDestinationUrl.value!!.value != "")) {
            dialog.findViewById<TextView>(R.id.sendAddressTextView)!!.text = getString(
                R.string.address_send_received_message,
                vm.messageDestinationUrl.value!!.value
            )
        } else {
            dialog.findViewById<TextView>(R.id.sendAddressHintTextView)!!.visibility = View.GONE
            dialog.findViewById<TextView>(R.id.sendAddressTextView)!!.visibility = View.GONE
        }

        dialog.findViewById<Button>(R.id.cancelButton)!!.setOnClickListener {
            dialog.dismiss()
        }

        dialog.setOnDismissListener {
            vm.onHowToUseDialogClose()
        }

        dialog.setOnCancelListener {
            vm.onHowToUseDialogClose()
        }

        dialog.show()
        vm.onHowToUseDialogOpen()
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
//                    return "123.456.0.78"
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

    override fun checkPermissions(): Boolean {
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
                android.Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            showAppFunctionalityInfoDialog()
            return false
        } else
            return true
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