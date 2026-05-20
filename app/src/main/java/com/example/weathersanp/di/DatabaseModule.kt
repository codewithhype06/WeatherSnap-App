// File: app/src/main/java/com/example/weathersnap/di/DatabaseModule.kt
package com.example.weathersnap.di

import android.content.Context
import androidx.room.Room
import com.example.weathersnap.data.local.ReportDao
import com.example.weathersnap.data.local.WeatherDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideWeatherDatabase(@ApplicationContext context: Context): WeatherDatabase {
        return Room.databaseBuilder(
            context,
            WeatherDatabase::class.java,
            "weather_snap_db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideReportDao(database: WeatherDatabase): ReportDao {
        return database.reportDao()
    }
}