package com.breakneck.sms_modem.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.breakneck.domain.model.IpAddress
import com.breakneck.domain.model.Message
import com.breakneck.domain.model.MessageDestinationUrl
import com.breakneck.domain.model.MessageFullListVisibilityState
import com.breakneck.domain.model.NetworkState
import com.breakneck.domain.model.Port
import com.breakneck.domain.model.ServiceBoundState
import com.breakneck.domain.model.ServiceIntent
import com.breakneck.domain.model.ServiceState
import com.breakneck.domain.usecase.message.GetAllMessages
import com.breakneck.domain.usecase.service.GetServiceRemainingTime
import com.breakneck.domain.usecase.settings.GetMessageDestinationUrl
import com.breakneck.domain.usecase.settings.GetPort
import com.breakneck.domain.usecase.service.GetServiceState
import com.breakneck.domain.usecase.service.SaveServiceRemainingTime
import com.breakneck.domain.usecase.settings.GetDeviceIpAddress
import com.breakneck.domain.usecase.settings.SaveMessageDestinationUrl
import com.breakneck.domain.usecase.settings.SavePort
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivityViewModel(
    private val savePort: SavePort,
    private val getPort: GetPort,
    private val getServiceState: GetServiceState,
    private val saveMessageDestinationUrl: SaveMessageDestinationUrl,
    private val getMessageDestinationUrl: GetMessageDestinationUrl,
    private val saveServiceRemainingTime: SaveServiceRemainingTime,
    private val getServiceRemainingTime: GetServiceRemainingTime,
    private val getDeviceIpAddress: GetDeviceIpAddress
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

    init {
        Log.e(TAG, "MainViewModel Created")
        getPort()
        getMessageDestinationUrl()
        changeServiceIntent()
        getServiceRemainingTime()
        getServiceIpAddress()
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

    fun changeServiceBoundState() {
        when (networkServiceBoundState.value!!) {
            ServiceBoundState.Bounded -> {
                _networkServiceBoundState.value = ServiceBoundState.Unbounded
            }
            ServiceBoundState.Unbounded -> {
                _networkServiceBoundState.value = ServiceBoundState.Bounded
            }
        }
    }

    fun saveServiceRemainingTime() {
        //TODO CHANGE TO HOURS
        saveServiceRemainingTime.execute(24000)
        getServiceRemainingTime()
    }

    fun getServiceRemainingTime() {
        _serviceRemainingTimeInSec.value = getServiceRemainingTime.execute() / 1000
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
}