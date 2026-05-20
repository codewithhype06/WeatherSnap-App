// File: app/src/main/java/com/example/weathersnap/presentation/savedreports/SavedReportsScreen.kt
package com.example.weathersnap.presentation.savedreports

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.weathersnap.presentation.components.WeatherDetailItem
import com.example.weathersnap.utils.ImageUtils
import java.io.File

@Composable
fun SavedReportsScreen(
    onBack: () -> Unit,
    viewModel: SavedReportsViewModel = hiltViewModel()
) {
    val reports by viewModel.reports.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFF121212)).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Button(onClick = onBack, colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)) {
                Text("Back")
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text("Saved Reports", style = MaterialTheme.typography.headlineMedium, color = Color(0xFFC5E1A5))
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (reports.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No reports saved yet.", color = Color.Gray)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(reports) { report ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2C))
                    ) {
                        Column {
                            AsyncImage(
                                model = File(report.imagePath),
                                contentDescription = "Captured Weather Image",
                                modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(report.city, color = Color.White, style = MaterialTheme.typography.titleLarge)
                                Text("${report.temperature}°C - ${report.condition}", color = Color.Gray)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(report.notes, color = Color.White)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Saved: ${ImageUtils.formatTimestamp(report.timestamp)}", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                                Text("Sizes: ${report.originalSizeKb}KB -> ${report.compressedSizeKb}KB", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
}