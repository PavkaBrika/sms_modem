package com.breakneck.sms_modem.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Build
import android.util.Log
import androidx.core.content.getSystemService
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.breakneck.domain.model.NetworkState

const val NETWORK_BROADCAST_STATE_RESULT = "NETWORK_BROADCAST_STATE_RESULT"
const val NETWORK_BROADCAST_STATE = "NETWORK_BROADCAST_STATE"

class NetworkChangeReceiver: BroadcastReceiver() {

    val TAG = "NetworkChangeReceiver"

    override fun onReceive(context: Context?, intent: Intent?) {
        try {
            val broadcaster = LocalBroadcastManager.getInstance(context!!)
            if (isOnline(context)) {
                Log.e(TAG, "Network connection is true")
                Intent(NETWORK_BROADCAST_STATE)
                    .putExtra(NETWORK_BROADCAST_STATE_RESULT, NetworkState.Available.toString())
                    .also {
                        broadcaster.sendBroadcast(it)
                    }
            } else {
                Log.e(TAG, "Network connection is false")
                Intent(NETWORK_BROADCAST_STATE)
                    .putExtra(NETWORK_BROADCAST_STATE_RESULT, NetworkState.Unavailable.toString())
                    .also {
                        broadcaster.sendBroadcast(it)
                    }
            }
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }
    }

    private fun isOnline(context: Context): Boolean {
        try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkState = connectivityManager.activeNetworkInfo
            return ((networkState != null) && (networkState.isConnected))
        } catch (e: NullPointerException) {
            e.printStackTrace()
            return false
        }
    }
}