package com.breakneck.sms_modem.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.breakneck.domain.usecase.GetPort
import com.breakneck.domain.usecase.SavePort

class MainViewModelFactory(
    private val savePort: SavePort,
    private val getPort: GetPort
) : ViewModelProvider.Factory {



    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainViewModel(
            savePort = savePort,
            getPort = getPort
        ) as T
    }

}