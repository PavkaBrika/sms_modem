package com.breakneck.sms_modem.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.breakneck.domain.TOTAL_ADS_QUANTITY
import com.breakneck.domain.model.IpAddress
import com.breakneck.domain.model.MessageDestinationUrl
import com.breakneck.domain.model.NetworkState
import com.breakneck.domain.model.Port
import com.breakneck.domain.model.RemainingAdsQuantity
import com.breakneck.domain.model.ServiceBoundState
import com.breakneck.domain.model.ServiceIntent
import com.breakneck.domain.model.ServiceState
import com.breakneck.domain.model.SubscriptionPlan
import com.breakneck.domain.usecase.service.GetServiceRemainingTimeInMillis
import com.breakneck.domain.usecase.settings.GetMessageDestinationUrl
import com.breakneck.domain.usecase.settings.GetPort
import com.breakneck.domain.usecase.service.GetServiceState
import com.breakneck.domain.usecase.service.SaveServiceRemainingTimeInMillis
import com.breakneck.domain.usecase.settings.GetDeviceIpAddress
import com.breakneck.domain.usecase.settings.GetRemainingAds
import com.breakneck.domain.usecase.settings.SaveMessageDestinationUrl
import com.breakneck.domain.usecase.settings.SavePort
import com.breakneck.domain.usecase.settings.SaveRemainingAds

class MainActivityViewModel(
    private val savePort: SavePort,
    private val getPort: GetPort,
    private val getServiceState: GetServiceState,
    private val saveMessageDestinationUrl: SaveMessageDestinationUrl,
    private val getMessageDestinationUrl: GetMessageDestinationUrl,
    private val saveServiceRemainingTimeInMillis: SaveServiceRemainingTimeInMillis,
    private val getServiceRemainingTimeInMillis: GetServiceRemainingTimeInMillis,
    private val getDeviceIpAddress: GetDeviceIpAddress,
    private val getRemainingAds: GetRemainingAds,
    private val saveRemainingAds: SaveRemainingAds
): ViewModel() {

    val TAG = "MainViewModel"

    private val _networkServiceState = MutableLiveData<ServiceState>()
    val networkServiceState: LiveData<ServiceState>
        get() = _networkServiceState

    private val _networkServiceBoundState = MutableLiveData<ServiceBoundState>(ServiceBoundState.Unbounded)
    val networkServiceBoundState: LiveData<ServiceBoundState>
        get() = _networkServiceBoundState

    private val _networkServiceIntent = MutableLiveData<ServiceIntent>()
    val networkServiceIntent: LiveData<ServiceIntent>
        get() = _networkServiceIntent

    private val _port = MutableLiveData<Port>()
    val port: LiveData<Port>
        get() = _port

    private val _messageDestinationUrl = MutableLiveData<MessageDestinationUrl>()
    val messageDestinationUrl: LiveData<MessageDestinationUrl>
        get() = _messageDestinationUrl

    private val _serviceRemainingTimeInSec = MutableLiveData<Long>()
    val serviceRemainingTime: LiveData<Long>
        get() = _serviceRemainingTimeInSec

    private val _serverIpAddress = MutableLiveData<IpAddress>()
    val serverIpAddress: LiveData<IpAddress>
        get() = _serverIpAddress

    private val _serviceError = MutableLiveData<String>()
    val serviceError: LiveData<String>
        get() = _serviceError

    private val _remainingAds = MutableLiveData<RemainingAdsQuantity>()
    val remainingAds: LiveData<RemainingAdsQuantity>
        get() = _remainingAds

    private val _networkState = MutableLiveData<NetworkState>(NetworkState.Available)
    val networkState: LiveData<NetworkState>
        get() = _networkState

    private val _selectedSubscription = MutableLiveData<SubscriptionPlan>(SubscriptionPlan.ANNUALLY)
    val selectedSubscription: LiveData<SubscriptionPlan>
        get() = _selectedSubscription

    private val _isSubscriptionDialogOpened = MutableLiveData<Boolean>(false)
    val isSubscriptionDialogOpened: LiveData<Boolean>
        get() = _isSubscriptionDialogOpened

    init {
        Log.e(TAG, "MainViewModel Created")
        getPort()
        getMessageDestinationUrl()
        changeServiceIntent()
        getServiceRemainingTime()
        getServiceIpAddress()
        getRemainingAds()
    }

    override fun onCleared() {
        super.onCleared()
        Log.e(TAG, "MainViewModel cleared")
    }

    private fun getPort() {
        _port.value = getPort.execute()
        Log.e(TAG, "Port got: ${port.value}")
    }

    fun savePort(port: Port) {
        if (this.port.value!! != port) {
            savePort.execute(port = port)
            Log.e(TAG, "Port saved: ${port.value}")
            _port.value = port
        } else {
            Log.e(TAG, "Port not saved: Equals old value")
        }

    }

    private fun getMessageDestinationUrl() {
        _messageDestinationUrl.value = getMessageDestinationUrl.execute()
        Log.e(TAG, "Message destination is: ${messageDestinationUrl.value}")
    }

    fun saveMessageDestinationUrl(url: MessageDestinationUrl) {
        if (this.messageDestinationUrl.value!! != url) {
            saveMessageDestinationUrl.execute(url = url)
            _messageDestinationUrl.value = url
            Log.e(TAG, "Message destination saved: ${messageDestinationUrl.value}")
        } else {
            Log.e(TAG, "Message destination not saved: Equals old value")
        }
    }

    fun getServiceState() {
        _networkServiceState.value = getServiceState.execute()
        Log.e(TAG, "Server is ${_networkServiceState.value}")
    }

    fun changeServiceIntent() {
        getServiceState()
        when (networkServiceState.value!!) {
            ServiceState.Enabled -> {
                _networkServiceIntent.value = ServiceIntent.Disable
            }
            ServiceState.Disabled -> {
                _networkServiceIntent.value = ServiceIntent.Enable
            }
            ServiceState.Loading -> {
                _networkServiceIntent.value = ServiceIntent.Enable
            }
        }
    }

    fun setServiceStateLoading() {
        _networkServiceState.value = ServiceState.Loading
    }

    fun onServiceBind() {
        _networkServiceBoundState.value = ServiceBoundState.Bounded
        Log.e(TAG, "Service bound state is ${networkServiceBoundState.value.toString()}")
    }

    fun onServiceUnbind() {
        _networkServiceBoundState.value = ServiceBoundState.Unbounded
        Log.e(TAG, "Service bound state is ${networkServiceBoundState.value.toString()}")
    }

    fun saveServiceRemainingTime() {
        //TODO CHANGE TO HOURS
        saveServiceRemainingTimeInMillis.execute(getServiceRemainingTimeInMillis.execute() + 24000)
        getServiceRemainingTime()
    }

    fun getServiceRemainingTime() {
        _serviceRemainingTimeInSec.value = getServiceRemainingTimeInMillis.execute() / 1000
    }

    fun getServiceIpAddress() {
        val ipAddress = getDeviceIpAddress.execute()
        if (!ipAddress.equals(""))
            _serverIpAddress.value = ipAddress
    }

    fun setDeviceIpAddress(address: IpAddress) {
        _serverIpAddress.value = address
    }

    fun setServiceError(error: String) {
        _serviceError.value = error
    }

    fun getRemainingAds() {
        val remainingAds = getRemainingAds.execute().value
        if (remainingAds >= TOTAL_ADS_QUANTITY)
            _remainingAds.value = RemainingAdsQuantity(value = TOTAL_ADS_QUANTITY)
        else
            _remainingAds.value = getRemainingAds.execute()
    }

    fun onAdView() {
        if (_remainingAds.value!!.value <= 0)
            _remainingAds.value = RemainingAdsQuantity(0)
        else
            _remainingAds.value = RemainingAdsQuantity(_remainingAds.value!!.value - 1)
        saveRemainingAds()
    }

    private fun saveRemainingAds() {
        saveRemainingAds.execute(_remainingAds.value!!)
    }

    fun onNetworkUnavailable() {
        _networkServiceIntent.value = ServiceIntent.Enable
        _networkServiceState.value = ServiceState.Disabled
        _networkState.value = NetworkState.Unavailable
    }

    fun onNetworkAvailable() {
        _networkServiceIntent.value = ServiceIntent.Enable
        _networkServiceState.value = ServiceState.Disabled
        _networkState.value = NetworkState.Available
    }

    fun onAnnualSubscriptionClicked() {
        _selectedSubscription.value = SubscriptionPlan.ANNUALLY
    }

    fun onSeasonSubscriptionClicked() {
        _selectedSubscription.value = SubscriptionPlan.SEASONALLY
    }

    fun onMonthSubscriptionClicked() {
        _selectedSubscription.value = SubscriptionPlan.MONTHLY
    }

    fun onSubscriptionDialogOpen() {
        _isSubscriptionDialogOpened.value = true
    }

    fun onSubscriptionDialogClose() {
        _isSubscriptionDialogOpened.value = false
    }
}