package com.example.weatherapp.domain.model

data class HourlyInfo(
    val time: String,
    val temp: Double,
    val feelsLike: Double,
    val iconEmoji: String,
    val isDay: Boolean
)
