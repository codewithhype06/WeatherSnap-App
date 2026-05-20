package com.example.weathersnap.di

import com.example.weathersnap.data.local.ReportDao
import com.example.weathersnap.data.remote.OpenMeteoApi
import com.example.weathersnap.data.repository.WeatherRepositoryImpl
import com.example.weathersnap.domain.repository.WeatherRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // ✅ FIX: Yahan se Database aur DAO dono hata diye kyunki wo DatabaseModule.kt mein hain!

    @Provides
    @Singleton
    fun provideWeatherRepository(
        api: OpenMeteoApi,
        dao: ReportDao
    ): WeatherRepository {
        return WeatherRepositoryImpl(api, dao)
    }
}