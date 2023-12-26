package com.breakneck.sms_modem.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel

class MainActivityViewModel: ViewModel() {

    init {

    }

    override fun onCleared() {
        super.onCleared()
        Log.e("ViewModel", "MainViewModel cleared")
    }
}