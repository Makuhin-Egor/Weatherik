package com.example.weatherapp.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {

    @GET("v1/forecast")
    suspend fun getWeather(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String = "temperature_2m,apparent_temperature,weather_code,is_day",
        @Query("daily") daily: String = "weather_code,temperature_2m_max,temperature_2m_min,sunrise,sunset",
        @Query("hourly") hourly: String = "temperature_2m,apparent_temperature,weather_code,is_day",
        @Query("timezone") timezone: String = "auto",
        @Query("forecast_days") forecastDays: Int = 10
    ): WeatherResponse
}
