package com.breakneck.sms_modem.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.breakneck.domain.usecase.message.DeleteAllMessages
import com.breakneck.domain.usecase.settings.SaveRemindNotificationTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class InfoFragmentViewModel(
    private val saveRemindNotificationTime: SaveRemindNotificationTime,
    private val deleteAllMessages: DeleteAllMessages
): ViewModel() {

    val TAG = "InfoFragmentViewModel"

    init {
        Log.e(TAG, "ViewModel Created")
    }

    override fun onCleared() {
        super.onCleared()
        Log.e(TAG, "ViewModel cleared")
    }

    fun saveRemindNotificationTime(timeInMillis: Long) {
        saveRemindNotificationTime.execute(timeInMillis)
    }

    fun deleteAllMessages() {
        viewModelScope.launch(Dispatchers.IO) {
            deleteAllMessages.execute()
        }
    }
}