package com.example.weathersnap.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class City(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val country: String,
    val admin1: String
) : Parcelable

@Parcelize
data class WeatherSnapshot(
    val cityName: String,
    val temperature: Double,
    val condition: String,
    val humidity: Int,
    val windSpeed: Double,
    val pressure: Double
) : Parcelable

data class WeatherReport(
    val id: Int = 0,
    val city: String,
    val temperature: Double,
    val condition: String,
    val humidity: Int,
    val windSpeed: Double,
    val pressure: Double,
    val notes: String,
    val imagePath: String,
    val originalSizeKb: Long,
    val compressedSizeKb: Long,
    val timestamp: Long
)