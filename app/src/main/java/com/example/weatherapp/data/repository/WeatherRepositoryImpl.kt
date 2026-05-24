package com.example.weatherapp.data.repository

import com.example.weatherapp.data.local.HourlyEntity
import com.example.weatherapp.data.local.WeatherDao
import com.example.weatherapp.data.local.WeatherEntity
import com.example.weatherapp.data.remote.GeocodingApi
import com.example.weatherapp.data.remote.WeatherApi
import com.example.weatherapp.domain.model.CityInfo
import com.example.weatherapp.domain.model.HourlyInfo
import com.example.weatherapp.domain.model.WeatherData
import com.example.weatherapp.domain.model.WeatherInfo
import com.example.weatherapp.domain.repository.WeatherRepository
import com.example.weatherapp.domain.util.weatherCodeToDescription
import com.example.weatherapp.domain.util.weatherCodeToIcon
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class WeatherRepositoryImpl @Inject constructor(
    private val api: WeatherApi,
    private val geocodingApi: GeocodingApi,
    private val dao: WeatherDao
) : WeatherRepository {

    override suspend fun getWeather(lat: Double, lon: Double, cityName: String): Result<WeatherData> {
        return try {
            val response = api.getWeather(latitude = lat, longitude = lon)

            val dailyEntities = response.daily.time.indices.map { i ->
                WeatherEntity(
                    date = response.daily.time[i],
                    cityName = cityName,
                    weatherCode = response.daily.weather_code[i],
                    tempMax = response.daily.temperature_2m_max[i],
                    tempMin = response.daily.temperature_2m_min[i]
                )
            }
            dao.deleteAll()
            dao.upsertAll(dailyEntities)

            val currentHourFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:00")
            val currentHourString = LocalDateTime.now().format(currentHourFormatter)

            val startIndex = response.hourly.time.indexOfFirst { it == currentHourString }.takeIf { it >= 0 } ?: 0
            val endIndex = minOf(startIndex + 24, response.hourly.time.size)

            val hourlyEntities = (startIndex until endIndex).map { i ->
                HourlyEntity(
                    time = response.hourly.time[i],
                    temp = response.hourly.temperature_2m[i],
                    feelsLike = response.hourly.apparent_temperature[i],
                    weatherCode = response.hourly.weather_code[i],
                    isDay = response.hourly.is_day[i] == 1
                )
            }
            dao.deleteAllHourly()
            dao.upsertHourly(hourlyEntities)

            val dailyResult = dailyEntities.map { it.toDomain() }
            val hourlyResult = hourlyEntities.map { it.toHourlyDomain() }

            val currentHourEntity = hourlyEntities.firstOrNull()

            Result.success(
                WeatherData(
                    currentTemp = currentHourEntity?.temp ?: response.current.temperature_2m,
                    feelsLike = currentHourEntity?.feelsLike ?: response.current.apparent_temperature,
                    currentWeatherCode = currentHourEntity?.weatherCode ?: response.current.weather_code,
                    currentIsDay = currentHourEntity?.isDay ?: (response.current.is_day == 1),
                    daily = dailyResult,
                    hourly = hourlyResult
                )
            )
        } catch (e: Exception) {
            val cachedDaily = dao.getAllWeather()
            val cachedHourly = dao.getAllHourly()
            if (cachedDaily.isNotEmpty() && cachedHourly.isNotEmpty()) {
                val currentHourEntity = cachedHourly.firstOrNull()
                Result.success(
                    WeatherData(
                        currentTemp = currentHourEntity?.temp ?: cachedDaily.first().tempMax,
                        feelsLike = currentHourEntity?.feelsLike ?: cachedDaily.first().tempMax,
                        currentWeatherCode = currentHourEntity?.weatherCode ?: cachedDaily.first().weatherCode,
                        currentIsDay = currentHourEntity?.isDay ?: true,
                        daily = cachedDaily.map { it.toDomain() },
                        hourly = cachedHourly.map { it.toHourlyDomain() }
                    )
                )
            } else {
                Result.failure(e)
            }
        }
    }

    override suspend fun searchCities(query: String): Result<List<CityInfo>> {
        return try {
            val response = geocodingApi.searchCities(name = query)
            Result.success(response.results.map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun WeatherEntity.toDomain() = WeatherInfo(
        date = date,
        weatherCode = weatherCode,
        tempMax = tempMax,
        tempMin = tempMin,
        description = weatherCodeToDescription(weatherCode),
        iconEmoji = weatherCodeToIcon(weatherCode, true)
    )

    private fun HourlyEntity.toHourlyDomain() = HourlyInfo(
        time = time,
        temp = temp,
        feelsLike = feelsLike,
        iconEmoji = weatherCodeToIcon(weatherCode, isDay),
        isDay = isDay
    )

    private fun com.example.weatherapp.data.remote.CityDto.toDomain() = CityInfo(
        id = id,
        name = name,
        latitude = latitude,
        longitude = longitude,
        country = country,
        region = admin1 ?: ""
    )
}
