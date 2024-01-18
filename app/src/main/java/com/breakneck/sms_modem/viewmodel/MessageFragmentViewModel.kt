package com.breakneck.sms_modem.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.breakneck.domain.model.Message
import com.breakneck.domain.usecase.message.GetAllMessages
import com.breakneck.domain.usecase.message.SaveSentMessage
import com.breakneck.domain.usecase.message.SendMessageToServer
import com.breakneck.domain.usecase.message.UpdateMessage
import com.breakneck.domain.usecase.settings.GetMessageDestinationUrl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MessageFragmentViewModel(
    private val getAllMessages: GetAllMessages,
    private val sendMessageToServer: SendMessageToServer,
    private val getMessageDestinationUrl: GetMessageDestinationUrl,
    private val updateMessage: UpdateMessage
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

    private val _isMessagesDeleted = MutableLiveData<Boolean>(false)
    val isMessagesDeleted: LiveData<Boolean>
        get() = _isMessagesDeleted

    fun getAllMessages() {
        viewModelScope.launch(Dispatchers.IO) {
            val messageList = getAllMessages.execute()
            withContext(Dispatchers.Main) {
                _messagesList.value = messageList
                changeIsMessageDeleted()
            }
        }
    }

    fun sendMessageToServer(message: Message) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                sendMessageToServer.execute(getMessageDestinationUrl.execute(), message = message)
                message.sent = true
                updateMessage.execute(message = message)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.invokeOnCompletion {
            getAllMessages()
        }
    }

    fun changeIsMessageDeleted() {
        if (isMessagesDeleted.value == true)
            _isMessagesDeleted.value = false
        else
            _isMessagesDeleted.value = true
    }
}