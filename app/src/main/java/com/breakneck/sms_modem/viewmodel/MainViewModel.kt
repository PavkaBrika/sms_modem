package com.breakneck.sms_modem.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.breakneck.domain.model.ServiceState

class MainViewModel: ViewModel() {

    val TAG = "MainViewModel"

    val networkServiceState = MutableLiveData<ServiceState>(ServiceState.disabled)

    init {
        Log.e(TAG, "MainViewModel Created")

    }

    override fun onCleared() {
        super.onCleared()
        Log.e(TAG, "MainViewModel cleared")
    }

}