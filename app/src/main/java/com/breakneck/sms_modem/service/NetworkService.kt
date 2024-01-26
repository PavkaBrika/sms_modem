package com.breakneck.sms_modem.service

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ResolveInfo
import android.graphics.Color
import android.os.Binder
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import android.os.SystemClock
import android.telephony.SmsManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.breakneck.domain.HOURS_1_IN_SECONDS
import com.breakneck.domain.HOURS_24_IN_SECONDS
import com.breakneck.domain.HOURS_3_IN_SECONDS
import com.breakneck.domain.HOURS_48_IN_SECONDS
import com.breakneck.domain.PART_ADS_QUANTITY
import com.breakneck.domain.TOTAL_ADS_QUANTITY
import com.breakneck.domain.model.IpAddress
import com.breakneck.domain.model.Message
import com.breakneck.domain.model.RemainingAdsQuantity
import com.breakneck.domain.model.Sender
import com.breakneck.domain.model.ServiceIntent
import com.breakneck.domain.model.ServiceState
import com.breakneck.domain.usecase.settings.GetPort
import com.breakneck.domain.usecase.message.SaveSentMessage
import com.breakneck.domain.usecase.service.GetServiceRemainingTimeInMillis
import com.breakneck.domain.usecase.service.SaveServiceRemainingTimeInMillis
import com.breakneck.domain.usecase.service.SaveServiceState
import com.breakneck.domain.usecase.settings.GetMessageDestinationUrl
import com.breakneck.domain.usecase.settings.GetRemainingAds
import com.breakneck.domain.usecase.settings.GetRemindNotificationTimeInMillis
import com.breakneck.domain.usecase.settings.SaveDeviceIpAddress
import com.breakneck.domain.usecase.settings.SaveRemainingAds
import com.breakneck.domain.usecase.util.FromTimestampToDateString
import com.breakneck.sms_modem.R
import com.breakneck.sms_modem.presentation.activity.MainActivity
import com.breakneck.sms_modem.receiver.SMSBroadcastReceiver
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.gson.gson
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine
import org.koin.android.ext.android.inject
import java.lang.StringBuilder
import java.net.BindException
import java.util.Locale
import java.util.concurrent.TimeUnit

const val SERVICE_STATE_RESULT = "com.breakneck.sms_modem.SERVICE_STATE_RESULT"
const val SERVICE_TIME_REMAINING_RESULT = "com.breakneck.sms_modem.SERVICE_TIME_REMAINING_RESULT"
const val SERVICE_NEW_MESSAGE = "com.breakneck.sms_modem.SERVICE_NEW_MESSAGE"
const val SERVICE_ERROR = "com.breakneck.sms_modem.SERVICE_ERROR"
const val SERVICE_START_SUCCESS = "com.breakneck.sms_modem.SERVICE_START_SUCCESS"
const val SERVICE_REMIND_LATER = "com.breakneck.sms_modem.REMIND_LATER"
const val SERVICE_UPDATE_ADS = "com.breakneck.sms_modem.SERVICE_UPDATE_ADS"

const val NEW_MESSAGE = "com.breakneck.sms_modem.NEW_MESSAGE"
const val ERROR = "com.breakneck.sms_modem.ERROR"

const val SERVICE_NOTIFICATION_ID = 213431121
const val REMINDER_NOTIFICATION_ID = 21341122
const val DISABLED_NOTIFICATION_ID = 21341123

open class NetworkService : Service() {

    //TODO implement dagger instead koin
//    @Inject
//    lateinit var getPort: GetPort
//    @Inject
//    lateinit var saveServiceState: SaveServiceState

    private val binder: IBinder = NetworkServiceBinder()
    private lateinit var broadcaster: LocalBroadcastManager

    val getPort: GetPort by inject()
    val saveServiceState: SaveServiceState by inject()
    val saveSentMessage: SaveSentMessage by inject()
    val getServiceRemainingTimeInMillis: GetServiceRemainingTimeInMillis by inject()
    val saveServiceRemainingTimeInMillis: SaveServiceRemainingTimeInMillis by inject()
    val saveDeviceIpAddress: SaveDeviceIpAddress by inject()
    val getMessageDestinationUrl: GetMessageDestinationUrl by inject()
    val getRemindNotificationTimeInMillis: GetRemindNotificationTimeInMillis by inject()
    val getRemainingAdsQuantity: GetRemainingAds by inject()
    val saveRemainingAdsQuantity: SaveRemainingAds by inject()


    private lateinit var server: NettyApplicationEngine
    private var serviceState: ServiceState = ServiceState.Disabled

    private lateinit var smsReceiver: SMSBroadcastReceiver
    private lateinit var intentFilter: IntentFilter
    private lateinit var timer: CountDownTimer
    private lateinit var ipAddress: String
    private lateinit var notificationManager: NotificationManager

    val notificationChannelId = "SMS_SERVICE_CHANNEL"

    var remindTimeInSec = getRemindNotificationTimeInMillis.execute() / 1000

    val TAG = "NetworkService"

    override fun onBind(intent: Intent?): IBinder? {
        Log.e(TAG, "Service bind")
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.e(TAG, "Service unbind")
        return super.onUnbind(intent)
    }

    override fun onRebind(intent: Intent?) {
        Log.e(TAG, "Service rebind")
        super.onRebind(intent)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.e(TAG, "onStartCommand executed with startId: $startId")
        if (intent != null) {
            val action = intent.action
            ipAddress = intent.extras?.getString("ipAddress").toString()
            if ((::ipAddress.isInitialized) && (ipAddress != "null")) {
                notificationManager.notify(SERVICE_NOTIFICATION_ID, createServiceNotification())
                saveDeviceIpAddress.execute(ipAddress = IpAddress(ipAddress))
            }
            try {
                Log.e(TAG, "using a intent with $action")
                when (action) {
                    ServiceIntent.Disable.toString() -> stopService()
                    ServiceIntent.Enable.toString() -> {
                        try {
                            startService()
                        } catch (e: BindException) {
                            e.printStackTrace()
                            sendErrorWithService(getString(R.string.unable_to_create_service_please_change_port_and_try_again))
                            stopService()
                        }
                    }

                    SERVICE_REMIND_LATER -> {
                        val serviceRemainingTimeInSeconds = getServiceRemainingTimeInMillis.execute() / 1000
                        if (serviceRemainingTimeInSeconds > HOURS_3_IN_SECONDS) {
                            remindTimeInSec = HOURS_3_IN_SECONDS
                        } else if (serviceRemainingTimeInSeconds > HOURS_1_IN_SECONDS) {
                            remindTimeInSec = HOURS_1_IN_SECONDS
                        }
                        notificationManager.cancel(REMINDER_NOTIFICATION_ID)
                    }
                }
            } catch (e: NullPointerException) {
                e.printStackTrace()
            }

        }
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        Log.e(TAG, "Service create")
        //TODO implement dagger instead koin
//        AndroidInjection.inject(this)
        broadcaster = LocalBroadcastManager.getInstance(this)

        createNotificationChannel()
        val notification: Notification = createServiceNotification()
        startForeground(SERVICE_NOTIFICATION_ID, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            server.stop(1000, 5000)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            unregisterReceiver(smsReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        saveDeviceIpAddress.execute(ipAddress = IpAddress(value = ""))
        Log.e(TAG, "Service destroyed")
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val restartServiceIntent = Intent(applicationContext, NetworkService::class.java).also {
            it.setPackage(packageName)
        }
        val restartServicePendingIntent =
            PendingIntent.getService(this, 1, restartServiceIntent, PendingIntent.FLAG_IMMUTABLE)
        val alarmService =
            applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmService.set(
            AlarmManager.ELAPSED_REALTIME,
            SystemClock.elapsedRealtime() + 1000,
            restartServicePendingIntent
        )
        super.onTaskRemoved(rootIntent)
    }

    fun startService() {
        if (serviceState is ServiceState.Enabled)
            return

        createServer()
        if (getMessageDestinationUrl.execute().value.isNotEmpty())
            createSmsReceiver()

        serviceState = ServiceState.Enabled
        saveServiceState.execute(serviceState)
        hideErrorCardInActivity()
        changeServiceStateInActivity()
        updateServiceRemainingTimer()
        notificationManager.cancel(DISABLED_NOTIFICATION_ID)

        Log.e(TAG, "Starting service task")
    }

    fun stopService() {
        Log.e(TAG, "Stopped service task")
        try {
            timer.cancel()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            serviceState = ServiceState.Disabled
            saveServiceState.execute(serviceState)
            changeServiceStateInActivity()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(STOP_FOREGROUND_REMOVE)
            } else {
                stopForeground(true)
            }
            notificationManager.cancel(SERVICE_NOTIFICATION_ID)
            notificationManager.cancel(REMINDER_NOTIFICATION_ID)
            stopSelf()
        } catch (e: Exception) {
            Log.e("TAG", "Service stopped without being started: ${e.message}")
            e.printStackTrace()
        }
    }

    fun createServer() {
        server = embeddedServer(Netty, port = getPort.execute().value) {
            install(ContentNegotiation) {
                gson()
            }
            routing {
                get("/") {
                    call.respondText("SERVICE ENABLED")
                }
                get("/{phone}/{message}") {
                    val phone = call.parameters["phone"]
                    val message = call.parameters["message"]
                    if ((phone != "") && (message != "")) {
                        val date = FromTimestampToDateString().execute(
                            System.currentTimeMillis(),
                            getCurrentLocale(applicationContext)
                        )
                        sendSMS(phoneNumber = phone!!, message = message!!, date = date)
                        //TODO return json code
                        call.respondText("$date: Message $message sent to $phone")
                    } else {
                        call.respondText("Error")
                    }
                }
            }
        }.start(wait = false)
        Log.e(TAG, "Server created")
    }

    fun createNotificationChannel() {
        notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                notificationChannelId,
                "SMS service",
                NotificationManager.IMPORTANCE_DEFAULT
            ).let {
                it.description = "SMS Service channel"
                it.enableLights(true)
                it.lightColor = Color.RED
                it
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun createServiceNotification(): Notification {
        val notificationClickPendingIntent =
            Intent(this, MainActivity::class.java)
                .let { notificationIntent ->
                    PendingIntent.getActivity(
                        this,
                        0,
                        notificationIntent,
                        PendingIntent.FLAG_MUTABLE
                    )
                }

        val notificationButtonPendingIntent =
            Intent(this, NetworkService::class.java)
                .apply { action = ServiceIntent.Disable.toString() }
                .let { notificationIntent ->
                    PendingIntent.getService(
                        this,
                        0,
                        notificationIntent,
                        PendingIntent.FLAG_MUTABLE
                    )
                }

        return NotificationCompat.Builder(this, notificationChannelId)
            .setSmallIcon(R.drawable.ic_notification_icon)
            .setContentTitle(getString(R.string.sms_service_is_running))
            .setContentText(
                if (this::ipAddress.isInitialized) {
                    getString(
                        R.string.with_ip,
                        ipAddress,
                        getPort.execute().value.toString()
                    )
                } else {
                    getString(R.string.unable_to_get_ip_address)
                }
            )
            .addAction(
                R.drawable.baseline_close_24,
                getString(R.string.stop_service),
                notificationButtonPendingIntent
            )
            .setOngoing(true)
            .setContentIntent(notificationClickPendingIntent)
            .build()
    }

    private fun createReminderNotification() {
        val notificationClickPendingIntent =
            Intent(this, MainActivity::class.java)
                .let { notificationIntent ->
                    PendingIntent.getActivity(
                        this,
                        0,
                        notificationIntent,
                        PendingIntent.FLAG_MUTABLE
                    )
                }

        val notification = NotificationCompat.Builder(this, notificationChannelId)
            .setSmallIcon(R.drawable.ic_notification_icon)
            .setContentTitle(
                getString(
                    R.string.service_will_stop_after_hours,
                    TimeUnit.MILLISECONDS.toHours(remindTimeInSec)
                )
            )
            .setContentText(getString(R.string.please_watch_ads_or_buy_a_subscription_to_continue_service_work))
            .setContentIntent(notificationClickPendingIntent)
            .setAutoCancel(true)

        if (remindTimeInSec >= HOURS_1_IN_SECONDS) {
            val notificationButtonPendingIntent =
                Intent(this, NetworkService::class.java)
                    .apply { action = SERVICE_REMIND_LATER }
                    .let { notificationIntent ->
                        PendingIntent.getService(
                            this,
                            0,
                            notificationIntent,
                            PendingIntent.FLAG_MUTABLE
                        )
                    }

            notification.addAction(
                R.drawable.baseline_close_24,
                getString(R.string.remind_later), notificationButtonPendingIntent
            )
        }

        notificationManager.notify(REMINDER_NOTIFICATION_ID, notification.build())
    }

    private fun createDisabledNotification() {
        val notificationClickPendingIntent =
            Intent(this, MainActivity::class.java)
                .let { notificationIntent ->
                    PendingIntent.getActivity(
                        this,
                        0,
                        notificationIntent,
                        PendingIntent.FLAG_MUTABLE
                    )
                }

        val notification = NotificationCompat.Builder(this, notificationChannelId)
            .setSmallIcon(R.drawable.ic_notification_icon)
            .setContentTitle(
                getString(R.string.sms_modem_service_has_stopped_working)
            )
            .setContentText(getString(R.string.please_watch_ads_or_buy_a_subscription_to_continue_service_work))
            .setContentIntent(notificationClickPendingIntent)
            .setAutoCancel(true)

        notificationManager.notify(DISABLED_NOTIFICATION_ID, notification.build())
    }

    private fun sendSMS(phoneNumber: String, message: String, date: String) {
        val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            applicationContext.getSystemService(SmsManager::class.java)
        } else {
            SmsManager.getDefault()
        }
        val messageParts = smsManager.divideMessage(message)
        smsManager.sendMultipartTextMessage(phoneNumber, null, messageParts, null, null)
        val messageText = StringBuilder("")
        for (text in messageParts) {
            messageText.append("$text ")
        }
        val message = Message(
            cellNumber = phoneNumber,
            text = messageText.toString(),
            date = date,
            sender = Sender.Server
        )
        saveSentMessage.execute(message = message)
        Log.e(TAG, "Message sent")
        addMessageItemToRecyclerView(item = message)
    }

    private fun createSmsReceiver() {
        smsReceiver = SMSBroadcastReceiver()
        intentFilter = IntentFilter().also {
            it.addAction("android.provider.Telephony.SMS_RECEIVED")
            it.priority = 2147482135
        }
        registerReceiver(smsReceiver, intentFilter)

        val intent = Intent("android.provider.Telephony.SMS_RECEIVED")
        val infos: List<ResolveInfo> = packageManager.queryBroadcastReceivers(intent, 0)
        for (info in infos) {
            Log.i(
                "TAG",
                "Receiver name:" + info.activityInfo.name.toString() + "; priority=" + info.priority
            )
        }
    }

    fun updateServiceRemainingTimer() {
        try {
            timer.cancel()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        timer = object : CountDownTimer(getServiceRemainingTimeInMillis.execute(), 1000) {

            override fun onTick(millisUntilFinished: Long) {
//                Log.e(TAG, "CountDownTimer second remaining until finished = $millisUntilFinished")
                val secondUntilFinished = millisUntilFinished / 1000
                if (remindTimeInSec != 0L)
                    if (secondUntilFinished == remindTimeInSec) {
                        createReminderNotification()
                    }
                when (secondUntilFinished) {
                    HOURS_24_IN_SECONDS -> {
                        saveRemainingAdsQuantity.execute(
                            RemainingAdsQuantity(
                                getRemainingAdsQuantity.execute().value + PART_ADS_QUANTITY
                            )
                        )
                        updateRemainingAdsQuantityInActivity()
                    }

                    HOURS_48_IN_SECONDS -> {
                        saveRemainingAdsQuantity.execute(
                            RemainingAdsQuantity(
                                getRemainingAdsQuantity.execute().value + PART_ADS_QUANTITY
                            )
                        )
                        updateRemainingAdsQuantityInActivity()
                    }
                }
                saveServiceRemainingTimeInMillis.execute(millisUntilFinished)
                updateServiceTimeRemainingInActivity()
            }

            override fun onFinish() {
                Log.e(TAG, "CountDownTimerFinished")
                saveServiceRemainingTimeInMillis.execute(0)
                saveRemainingAdsQuantity.execute(RemainingAdsQuantity(TOTAL_ADS_QUANTITY))
                updateRemainingAdsQuantityInActivity()
                createDisabledNotification()
                stopService()
            }
        }.start()
    }

    private fun getCurrentLocale(context: Context): Locale {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.resources.configuration.locales.get(0);
        } else {
            context.resources.configuration.locale;
        }
    }

    fun changeServiceStateInActivity() {
        Intent(SERVICE_STATE_RESULT)
            .also {
                broadcaster.sendBroadcast(it)
            }
    }

    fun updateServiceTimeRemainingInActivity() {
        Intent(SERVICE_TIME_REMAINING_RESULT)
            .also {
                broadcaster.sendBroadcast(it)
            }
    }

    fun addMessageItemToRecyclerView(item: Message) {
        Intent(SERVICE_NEW_MESSAGE)
            .putExtra(NEW_MESSAGE, item)
            .also {
                broadcaster.sendBroadcast(it)
            }
    }

    fun sendErrorWithService(error: String) {
        Intent(SERVICE_ERROR)
            .putExtra(ERROR, error)
            .also {
                broadcaster.sendBroadcast(it)
            }
    }

    fun hideErrorCardInActivity() {
        Intent(SERVICE_START_SUCCESS)
            .also {
                broadcaster.sendBroadcast(it)
            }
    }

    fun updateRemainingAdsQuantityInActivity() {
        Intent(SERVICE_UPDATE_ADS)
            .also {
                broadcaster.sendBroadcast(it)
            }
    }

    inner class NetworkServiceBinder : Binder() {
        fun getService(): NetworkService {
            return this@NetworkService
        }
    }

}