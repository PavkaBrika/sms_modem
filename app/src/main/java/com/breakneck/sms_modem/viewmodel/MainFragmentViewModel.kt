package com.breakneck.sms_modem.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.breakneck.domain.model.NetworkState

class MainFragmentViewModel: ViewModel() {

    val TAG = "FragmentViewModel"

    private val _networkState = MutableLiveData<NetworkState>(NetworkState.Available)
    val networkState: LiveData<NetworkState>
        get() = _networkState

    init {
        Log.e(TAG, "ViewModel Created")
    }

    override fun onCleared() {
        super.onCleared()
        Log.e(TAG, "ViewModel cleared")
    }

    fun changeNetworkState() {
        when (_networkState.value!!) {
            NetworkState.Available ->
                _networkState.value = NetworkState.Unavailable
            NetworkState.Unavailable ->
                _networkState.value = NetworkState.Available
        }
    }

}