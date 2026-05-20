package com.example.weathersnap.domain.repository

import com.example.weathersnap.domain.model.City
import com.example.weathersnap.domain.model.WeatherReport
import com.example.weathersnap.domain.model.WeatherSnapshot
import kotlinx.coroutines.flow.Flow // ✅ Flow import karna zaroori hai

interface WeatherRepository {
    suspend fun getWeather(city: City): Result<WeatherSnapshot>
    suspend fun saveReport(report: WeatherReport)
    fun getAllReports(): Flow<List<WeatherReport>> // ✅ YEH NAYA FUNCTION
}