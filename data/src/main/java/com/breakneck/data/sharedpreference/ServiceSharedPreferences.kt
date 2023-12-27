package com.breakneck.data.sharedpreference

import android.content.Context
import com.breakneck.data.entity.ServiceStateData
import com.breakneck.data.storage.ServiceStorage

const val SERVICE_SHARED_PREFERENCES_NAME = "SERVICE_SHARED_PREFERENCES"
const val SERVICE_STATE = "SERVICE_STATE"

class ServiceSharedPreferences(context: Context): ServiceStorage {

    val sharedPreferences = context.getSharedPreferences(SERVICE_SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)

    override fun saveServiceState(serviceState: ServiceStateData) {
        sharedPreferences
            .edit()
            .putString(
                SERVICE_STATE,
                when (serviceState) {
                    ServiceStateData.disabled -> "disabled"
                    ServiceStateData.enabled -> "enabled"
                }
            )
            .apply()
    }

    override fun getServiceState(): ServiceStateData {
        return when (sharedPreferences.getString(SERVICE_STATE, "disabled")) {
            "disabled" -> ServiceStateData.disabled
            "enabled" -> ServiceStateData.enabled
            else -> ServiceStateData.disabled
        }
    }
}