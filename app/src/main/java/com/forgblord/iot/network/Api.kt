package com.forgblord.iot.network

import com.forgblord.iot.models.ConfigRequest
import com.forgblord.iot.models.Message
import com.forgblord.iot.models.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Url

interface Api {
    @GET("/")
    suspend fun requestData(): Response

    @GET("/water")
    suspend fun requestWater(): Response

    @GET("/config")
    suspend fun requestConfig(): ConfigRequest

    @GET("/change")
    suspend fun postConfig(
        @Query("waterMax") waterMax: Float,
        @Query("soilMin") soilMin: Int,
        @Query("soilMax") soilMax: Int,
        @Query("moistThreshold") moistThreshold: Int,
        @Query("checkInterval") checkInterval: Int,
        @Query("wateringDuration") wateringDuration: Int
    ): Message

    @GET
    suspend fun test(@Url url: String): Message
}