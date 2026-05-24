package com.example.weatherapp.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface WeatherDao {

    @Query("SELECT * FROM weather ORDER BY date ASC")
    suspend fun getAllWeather(): List<WeatherEntity>

    @Upsert
    suspend fun upsertAll(weather: List<WeatherEntity>)

    @Query("DELETE FROM weather")
    suspend fun deleteAll()

    @Upsert
    suspend fun upsertHourly(weather: List<HourlyEntity>)

    @Query("SELECT * FROM hourly ORDER BY time ASC")
    suspend fun getAllHourly(): List<HourlyEntity>

    @Query("DELETE FROM hourly")
    suspend fun deleteAllHourly()
}
