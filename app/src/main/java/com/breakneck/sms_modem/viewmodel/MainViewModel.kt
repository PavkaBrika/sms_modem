package com.breakneck.sms_modem.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.breakneck.domain.model.Port
import com.breakneck.domain.model.ServiceState
import com.breakneck.domain.usecase.GetPort
import com.breakneck.domain.usecase.SavePort

class MainViewModel(
    private val savePort: SavePort,
    private val getPort: GetPort
): ViewModel() {

    val TAG = "MainViewModel"

    private val _networkServiceState = MutableLiveData<ServiceState>(ServiceState.Disabled)
    val networkServiceState: LiveData<ServiceState>
        get() = _networkServiceState

    private val _port = MutableLiveData<Port>()
    val port: LiveData<Port>
        get() = _port

    init {
        Log.e(TAG, "MainViewModel Created")
        getPort()
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
        savePort.execute(port = port)
        Log.e(TAG, "Port saved: ${port.value}")
        _port.value = port
    }

    fun changeServiceState() {
        when (networkServiceState.value!!) {
            ServiceState.Enabled -> {
                _networkServiceState.value = ServiceState.Disabled
            }

            ServiceState.Disabled -> {
                _networkServiceState.value = ServiceState.Enabled
            }
        }
    }

}