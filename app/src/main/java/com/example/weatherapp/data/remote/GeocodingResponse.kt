package com.example.weatherapp.data.remote

data class GeocodingResponse(
    val results: List<CityDto> = emptyList()
)

data class CityDto(
    val id: Int,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val country: String,
    val admin1: String? = ""
)
