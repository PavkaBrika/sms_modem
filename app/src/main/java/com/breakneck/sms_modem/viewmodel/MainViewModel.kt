package com.breakneck.sms_modem.viewmodel

import android.util.Log
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

    val networkServiceState = MutableLiveData<ServiceState>(ServiceState.disabled)
    val port = MutableLiveData<Port>()

    init {
        Log.e(TAG, "MainViewModel Created")
        getPort()
    }

    override fun onCleared() {
        super.onCleared()
        Log.e(TAG, "MainViewModel cleared")
    }

    private fun getPort() {
        port.value = getPort.execute()
        Log.e(TAG, "Port got: ${port.value}")
    }

    fun savePort(port: Port) {
        savePort.execute(port = port)
        Log.e(TAG, "Port saved: ${port.value}")
        getPort()
    }

}