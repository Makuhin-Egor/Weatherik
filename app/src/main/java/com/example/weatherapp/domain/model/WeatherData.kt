package com.example.weatherapp.domain.model

data class WeatherData(
    val currentTemp: Double,
    val feelsLike: Double,
    val currentWeatherCode: Int,
    val currentIsDay: Boolean,
    val daily: List<WeatherInfo>,
    val hourly: List<HourlyInfo>
)
