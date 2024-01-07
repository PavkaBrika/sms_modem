package com.breakneck.sms_modem.di.koin

import com.breakneck.domain.usecase.service.GetServiceRemainingTime
import com.breakneck.domain.usecase.service.GetServiceState
import com.breakneck.domain.usecase.service.SaveServiceRemainingTime
import com.breakneck.domain.usecase.service.SaveServiceState
import org.koin.dsl.module

val serviceModule = module {

    factory<GetServiceState> {
        GetServiceState(serviceRepository = get())
    }

    factory<SaveServiceState> {
        SaveServiceState(serviceRepository = get())
    }

    factory<GetServiceRemainingTime> {
        GetServiceRemainingTime(serviceRepository = get())
    }

    factory<SaveServiceRemainingTime> {
        SaveServiceRemainingTime(serviceRepository = get())
    }
}