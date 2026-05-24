package com.example.weatherapp.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface GeocodingApi {

    @GET("v1/search")
    suspend fun searchCities(
        @Query("name") name: String,
        @Query("count") count: Int = 5,
        @Query("language") language: String = "en",
        @Query("format") format: String = "json"
    ): GeocodingResponse
}
