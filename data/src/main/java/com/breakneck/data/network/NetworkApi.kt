package com.breakneck.data.network

import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Url

interface NetworkApi {

    @POST
    suspend fun pushMessage(
        @Url url: String,
        @Query("cellNumber") cellNumber: String,
        @Query("text") text: String
    )
}