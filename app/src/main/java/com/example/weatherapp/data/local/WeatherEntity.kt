package com.example.weatherapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weather")
data class WeatherEntity(
    @PrimaryKey val date: String,
    val cityName: String,
    val weatherCode: Int,
    val tempMax: Double,
    val tempMin: Double
)
