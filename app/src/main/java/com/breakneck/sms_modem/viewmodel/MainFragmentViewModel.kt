package com.breakneck.sms_modem.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.breakneck.domain.model.NetworkState

class MainFragmentViewModel: ViewModel() {

    val TAG = "FragmentViewModel"

    private val _isRewardAdLoaded = MutableLiveData<Boolean>(false)
    val isRewardAdLoaded: LiveData<Boolean>
        get() = _isRewardAdLoaded

    init {
        Log.e(TAG, "ViewModel Created")
    }

    override fun onCleared() {
        super.onCleared()
        Log.e(TAG, "ViewModel cleared")
    }

    fun isRewardAdLoading() {
        _isRewardAdLoaded.value = false
    }

    fun isRewardAdLoadSuccess() {
        _isRewardAdLoaded.value = true
    }
}