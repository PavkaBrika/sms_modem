package com.breakneck.data.sharedpreference

import android.content.Context
import com.breakneck.data.entity.ServiceStateData
import com.breakneck.data.storage.ServiceStorage
import com.breakneck.domain.MINUTES_30_IN_SECONDS

private const val SERVICE_SHARED_PREFERENCES_NAME = "SERVICE_SHARED_PREFERENCES"
private const val SERVICE_STATE = "SERVICE_STATE"
private const val SERVICE_REMAINING_TIME = "SERVICE_REMAINING_TIME"

class ServiceSharedPreferences(private val context: Context): ServiceStorage {

    private val sharedPreferences = context.getSharedPreferences(SERVICE_SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)

    override fun saveServiceState(serviceState: ServiceStateData) {
        sharedPreferences
            .edit()
            .putString(
                SERVICE_STATE,
                when (serviceState) {
                    ServiceStateData.Disabled -> "disabled"
                    ServiceStateData.Enabled -> "enabled"
                }
            )
            .apply()
    }

    override fun getServiceState(): ServiceStateData {
        return when (sharedPreferences.getString(SERVICE_STATE, "disabled")) {
            "disabled" -> ServiceStateData.Disabled
            "enabled" -> ServiceStateData.Enabled
            else -> ServiceStateData.Disabled
        }
    }

    override fun getServiceRemainingTime(): Long {
        return sharedPreferences.getLong(SERVICE_REMAINING_TIME, MINUTES_30_IN_SECONDS)
    }

    override fun saveServiceRemainingTime(time: Long) {
        sharedPreferences.edit().putLong(SERVICE_REMAINING_TIME, time).apply()
    }
}