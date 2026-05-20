// File: app/src/main/java/com/example/weathersnap/data/local/ReportEntity.kt
package com.example.weathersnap.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weather_reports")
data class ReportEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
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