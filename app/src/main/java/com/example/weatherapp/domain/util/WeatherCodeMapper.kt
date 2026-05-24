package com.example.weatherapp.domain.util

fun weatherCodeToDescription(code: Int): String = when (code) {
    0 -> "Clear"
    1 -> "Mainly Clear"
    2 -> "Partly Cloudy"
    3 -> "Overcast"
    45, 48 -> "Fog"
    51, 53, 55 -> "Drizzle"
    56, 57 -> "Freezing Drizzle"
    61, 63, 65 -> "Rain"
    66, 67 -> "Freezing Rain"
    71, 73, 75 -> "Snowfall"
    77 -> "Snow Grains"
    80, 81, 82 -> "Rain Showers"
    85, 86 -> "Snow Showers"
    95 -> "Thunderstorm"
    96, 99 -> "Thunderstorm with Hail"
    else -> "Unknown"
}

fun weatherCodeToIcon(code: Int, isDay: Boolean = true): String = when (code) {
    0 -> if (isDay) "☀️" else "🌙"
    1 -> if (isDay) "🌤" else "🌙"
    2 -> if (isDay) "⛅" else "☁️"
    3 -> "☁️"
    45, 48 -> "🌫"
    51, 53, 55 -> "🌦"
    56, 57 -> "🌧"
    61, 63, 65 -> "🌧"
    66, 67 -> "🌧"
    71, 73, 75 -> "❄️"
    77 -> "🌨"
    80, 81, 82 -> "🌧"
    85, 86 -> "🌨"
    95 -> "⛈"
    96, 99 -> "⛈"
    else -> if (isDay) "☀️" else "🌙"
}
