package com.breakneck.sms_modem.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.breakneck.domain.model.MessageDestinationUrl
import com.breakneck.domain.model.Port
import com.breakneck.domain.model.ServiceBoundState
import com.breakneck.domain.model.ServiceIntent
import com.breakneck.domain.model.ServiceState
import com.breakneck.domain.usecase.GetMessageDestinationUrl
import com.breakneck.domain.usecase.GetPort
import com.breakneck.domain.usecase.GetServiceState
import com.breakneck.domain.usecase.SaveMessageDestinationUrl
import com.breakneck.domain.usecase.SavePort

class MainViewModel(
    private val savePort: SavePort,
    private val getPort: GetPort,
    private val getServiceState: GetServiceState,
    private val saveMessageDestinationUrl: SaveMessageDestinationUrl,
    private val getMessageDestinationUrl: GetMessageDestinationUrl
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

    init {
        Log.e(TAG, "MainViewModel Created")
        getPort()
        getMessageDestinationUrl()
        changeServiceIntent()
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

}