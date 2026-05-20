package com.example.weathersnap.presentation.weather

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

// ✅ EXACT IMPORTS
import com.example.weathersnap.domain.model.City
import com.example.weathersnap.domain.model.WeatherSnapshot
import com.example.weathersnap.presentation.components.WeatherSnapshotCard

@Composable
fun WeatherScreen(
    onNavigateToCreateReport: (WeatherSnapshot) -> Unit,
    onNavigateToReports: () -> Unit,
    viewModel: WeatherViewModel = hiltViewModel()
) {
    val query by viewModel.searchQuery.collectAsStateWithLifecycle()
    val suggestions by viewModel.suggestions.collectAsStateWithLifecycle()
    val weatherState by viewModel.weatherState.collectAsStateWithLifecycle()

    // 🚨 FIX: Explicitly forcing the type so the compiler stops crying
    @Suppress("UNCHECKED_CAST")
    val safeSuggestions: List<City> = (suggestions as? List<*>)?.filterIsInstance<City>() ?: emptyList()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(16.dp)
    ) {
        Text("WeatherSnap", style = MaterialTheme.typography.headlineLarge, color = Color(0xFFC5E1A5))
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = query,
            onValueChange = { viewModel.onQueryChange(it) },
            label = { Text("Search city...") },
            supportingText = { Text("Enter 3+ characters for suggestions") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )

        AnimatedVisibility(visible = safeSuggestions.isNotEmpty()) {
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                    // 🚨 FIX: Explicitly defined 'city: City'
                    items(items = safeSuggestions) { city: City ->
                        Text(
                            text = "${city.name}, ${city.country}",
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.selectCity(city) }
                                .padding(16.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        when (val state = weatherState) {
            is WeatherState.Loading -> Text("Loading weather...", color = Color(0xFFC5E1A5), modifier = Modifier.padding(16.dp))
            is WeatherState.Success -> {
                Column {
                    WeatherSnapshotCard(snapshot = state.data)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { onNavigateToCreateReport(state.data) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC5E1A5))
                    ) {
                        Text("Create Report", color = Color.Black)
                    }
                }
            }
            is WeatherState.Error -> Text("Error: ${state.message}", color = Color.Red)
            else -> {}
        }

        Spacer(modifier = Modifier.weight(1f))
        TextButton(onClick = onNavigateToReports, modifier = Modifier.fillMaxWidth()) {
            Text("View Saved Reports", color = Color(0xFFC5E1A5))
        }
    }
}