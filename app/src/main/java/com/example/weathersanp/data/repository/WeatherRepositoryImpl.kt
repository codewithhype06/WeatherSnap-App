package com.example.weathersnap.data.repository

import com.example.weathersnap.data.local.ReportDao
import com.example.weathersnap.data.local.ReportEntity
import com.example.weathersnap.data.remote.OpenMeteoApi
import com.example.weathersnap.domain.model.City
import com.example.weathersnap.domain.model.WeatherReport
import com.example.weathersnap.domain.model.WeatherSnapshot
import com.example.weathersnap.domain.repository.WeatherRepository
import kotlinx.coroutines.flow.Flow // ✅ Naya Import
import kotlinx.coroutines.flow.map // ✅ Naya Import
import javax.inject.Inject

class WeatherRepositoryImpl @Inject constructor(
    private val api: OpenMeteoApi,
    private val dao: ReportDao
) : WeatherRepository {

    override suspend fun getWeather(city: City): Result<WeatherSnapshot> {
        return try {
            val response = api.getWeather(
                latitude = city.latitude,
                longitude = city.longitude
            )

            val current = response.current
            val snapshot = WeatherSnapshot(
                cityName = city.name,
                temperature = current.temperature2m,
                condition = mapWeatherCode(current.weatherCode),
                humidity = current.relativeHumidity2m,
                windSpeed = current.windspeed10m,
                pressure = current.surfacePressure ?: 0.0
            )

            Result.success(value = snapshot)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveReport(report: WeatherReport) {
        val entity = ReportEntity(
            city = report.city,
            temperature = report.temperature,
            condition = report.condition,
            humidity = report.humidity,
            windSpeed = report.windSpeed,
            pressure = report.pressure,
            notes = report.notes,
            imagePath = report.imagePath,
            originalSizeKb = report.originalSizeKb,
            compressedSizeKb = report.compressedSizeKb,
            timestamp = report.timestamp
        )
        // Note: Agar dao mein insert function ka naam alag hai, toh yahan fix kar lena
        dao.insertReport(entity)
    }

    // ✅ YEH POORA NAYA BLOCK ADD KIYA HAI
    override fun getAllReports(): Flow<List<WeatherReport>> {
        // DAO se Entity ki list aayegi, usko Domain Model mein convert kar rahe hain
        return dao.getAllReports().map { entities ->
            entities.map { entity ->
                WeatherReport(
                    id = entity.id,
                    city = entity.city,
                    temperature = entity.temperature,
                    condition = entity.condition,
                    humidity = entity.humidity,
                    windSpeed = entity.windSpeed,
                    pressure = entity.pressure,
                    notes = entity.notes,
                    imagePath = entity.imagePath,
                    originalSizeKb = entity.originalSizeKb,
                    compressedSizeKb = entity.compressedSizeKb,
                    timestamp = entity.timestamp
                )
            }
        }
    }

    private fun mapWeatherCode(code: Int): String {
        return when (code) {
            0 -> "Clear sky"
            1, 2, 3 -> "Mainly clear, partly cloudy, and overcast"
            45, 48 -> "Fog and depositing rime fog"
            51, 53, 55 -> "Drizzle: Light, moderate, and dense intensity"
            56, 57 -> "Freezing Drizzle: Light and dense intensity"
            61, 63, 65 -> "Rain: Slight, moderate and heavy intensity"
            66, 67 -> "Freezing Rain: Light and heavy intensity"
            71, 73, 75 -> "Snow fall: Slight, moderate, and heavy intensity"
            77 -> "Snow grains"
            80, 81, 82 -> "Rain showers: Slight, moderate, and violent"
            85, 86 -> "Snow showers slight and heavy"
            95 -> "Thunderstorm: Slight or moderate"
            96, 99 -> "Thunderstorm with slight and heavy hail"
            else -> "Unknown"
        }
    }
}