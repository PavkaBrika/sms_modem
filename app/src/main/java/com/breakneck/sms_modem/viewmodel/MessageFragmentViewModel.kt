package com.breakneck.sms_modem.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.breakneck.domain.model.Message
import com.breakneck.domain.usecase.message.GetAllMessages
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MessageFragmentViewModel(
    private val getAllMessages: GetAllMessages
): ViewModel() {

    val TAG = "MessageViewModel"

    init {
        Log.e(TAG, "ViewModel created")
        getAllMessages()
    }

    override fun onCleared() {
        super.onCleared()
        Log.e(TAG, "ViewModel cleared")
    }

    private val _messagesList = MutableLiveData<List<Message>>()
    val messageList: LiveData<List<Message>>
        get() = _messagesList

    fun getAllMessages() {
        viewModelScope.launch(Dispatchers.IO) {
            val messageList = getAllMessages.execute()
            withContext(Dispatchers.Main) {
                _messagesList.value = messageList
            }
        }
    }
}