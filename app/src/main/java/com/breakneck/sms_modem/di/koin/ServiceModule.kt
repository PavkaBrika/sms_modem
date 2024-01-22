package com.breakneck.sms_modem.di.koin

import com.breakneck.domain.usecase.service.GetServiceRemainingTimeInMillis
import com.breakneck.domain.usecase.service.GetServiceState
import com.breakneck.domain.usecase.service.SaveServiceRemainingTimeInMillis
import com.breakneck.domain.usecase.service.SaveServiceState
import org.koin.dsl.module

val serviceModule = module {

    factory<GetServiceState> {
        GetServiceState(serviceRepository = get())
    }

    factory<SaveServiceState> {
        SaveServiceState(serviceRepository = get())
    }

    factory<GetServiceRemainingTimeInMillis> {
        GetServiceRemainingTimeInMillis(serviceRepository = get())
    }

    factory<SaveServiceRemainingTimeInMillis> {
        SaveServiceRemainingTimeInMillis(serviceRepository = get())
    }
}