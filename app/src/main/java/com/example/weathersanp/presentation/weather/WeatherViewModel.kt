package com.example.weathersnap.presentation.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// ✅ EXACT IMPORTS
import com.example.weathersnap.domain.model.City
import com.example.weathersnap.domain.model.WeatherSnapshot
import com.example.weathersnap.domain.repository.WeatherRepository
import com.example.weathersnap.data.remote.OpenMeteoApi

sealed class WeatherState {
    object Initial : WeatherState()
    object Loading : WeatherState()
    data class Success(val data: WeatherSnapshot) : WeatherState()
    data class Error(val message: String) : WeatherState()
}

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val repository: WeatherRepository,
    private val api: OpenMeteoApi
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _suggestions = MutableStateFlow<List<City>>(emptyList())
    val suggestions: StateFlow<List<City>> = _suggestions.asStateFlow()

    private val _weatherState = MutableStateFlow<WeatherState>(WeatherState.Initial)
    val weatherState: StateFlow<WeatherState> = _weatherState.asStateFlow()

    fun onQueryChange(query: String) {
        _searchQuery.value = query
        if (query.length >= 3) {
            searchCities(query)
        } else {
            _suggestions.value = emptyList()
        }
    }

    private fun searchCities(query: String) {
        viewModelScope.launch {
            try {
                val response = api.searchCities(name = query)

                // 🚨 FIX: Mapping GeocodingResult to City domain model
                _suggestions.value = response.results?.map { result ->
                    City(
                        name = result.name,
                        latitude = result.latitude,
                        longitude = result.longitude,
                        // Elvis operator (?: "") ensure karta hai ki agar API se country null aaye toh crash na ho
                        country = result.country ?: "",
                        admin1 = result.admin1 ?: ""
                    )
                } ?: emptyList()

            } catch (e: Exception) {
                _suggestions.value = emptyList()
            }
        }
    }

    fun selectCity(city: City) {
        _searchQuery.value = "${city.name}, ${city.country}"
        _suggestions.value = emptyList()
        fetchWeather(city)
    }

    private fun fetchWeather(city: City) {
        viewModelScope.launch {
            _weatherState.value = WeatherState.Loading

            val result = repository.getWeather(city)

            result.fold(
                onSuccess = { snapshot ->
                    _weatherState.value = WeatherState.Success(snapshot)
                },
                onFailure = { error ->
                    _weatherState.value = WeatherState.Error(error.localizedMessage ?: "Failed to fetch weather")
                }
            )
        }
    }
}