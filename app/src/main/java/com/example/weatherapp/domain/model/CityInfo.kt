package com.example.weatherapp.domain.model

data class CityInfo(
    val id: Int,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val country: String,
    val region: String
)
