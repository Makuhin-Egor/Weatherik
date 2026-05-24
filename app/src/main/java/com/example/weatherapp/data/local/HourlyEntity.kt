package com.example.weatherapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "hourly")
data class HourlyEntity(
    @PrimaryKey val time: String,
    val temp: Double,
    val feelsLike: Double,
    val weatherCode: Int,
    val isDay: Boolean
)
