package com.example.weatherapp.data.remote

data class WeatherResponse(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val timezone: String = "",
    val current: CurrentWeather = CurrentWeather(),
    val daily: DailyWeather = DailyWeather(),
    val hourly: HourlyWeather = HourlyWeather()
)

data class CurrentWeather(
    val temperature_2m: Double = 0.0,
    val apparent_temperature: Double = 0.0,
    val weather_code: Int = 0,
    val is_day: Int = 1
)

data class DailyWeather(
    val time: List<String> = emptyList(),
    val weather_code: List<Int> = emptyList(),
    val temperature_2m_max: List<Double> = emptyList(),
    val temperature_2m_min: List<Double> = emptyList()
)

data class HourlyWeather(
    val time: List<String> = emptyList(),
    val temperature_2m: List<Double> = emptyList(),
    val apparent_temperature: List<Double> = emptyList(),
    val weather_code: List<Int> = emptyList(),
    val is_day: List<Int> = emptyList()
)
