// File: app/src/main/java/com/example/weathersnap/data/local/WeatherDatabase.kt
package com.example.weathersnap.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [ReportEntity::class],
    version = 1,
    exportSchema = false
)
abstract class WeatherDatabase : RoomDatabase() {
    abstract fun reportDao(): ReportDao
}