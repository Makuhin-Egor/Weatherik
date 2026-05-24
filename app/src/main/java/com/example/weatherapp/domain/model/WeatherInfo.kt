package com.example.weatherapp.domain.model

data class WeatherInfo(
    val date: String,
    val weatherCode: Int,
    val tempMax: Double,
    val tempMin: Double,
    val description: String,
    val iconEmoji: String
)
