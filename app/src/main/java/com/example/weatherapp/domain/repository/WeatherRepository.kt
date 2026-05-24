package com.example.weatherapp.domain.repository

import com.example.weatherapp.domain.model.CityInfo
import com.example.weatherapp.domain.model.HourlyInfo
import com.example.weatherapp.domain.model.WeatherData
import com.example.weatherapp.domain.model.WeatherInfo

data class WeatherData(
    val currentTemp: Double,
    val feelsLike: Double,
    val currentWeatherCode: Int,
    val currentIsDay: Boolean,
    val daily: List<WeatherInfo>,
    val hourly: List<HourlyInfo>,
    val sunrise: String,
    val sunset: String
)

interface WeatherRepository {
    suspend fun getWeather(lat: Double, lon: Double, cityName: String): Result<WeatherData>
    suspend fun searchCities(query: String): Result<List<CityInfo>>
}
