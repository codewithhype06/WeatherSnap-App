package com.example.weathersnap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.weathersnap.domain.model.WeatherSnapshot // ✅ Import Ensure Kiya
import com.example.weathersnap.presentation.camera.CameraScreen
import com.example.weathersnap.presentation.report.CreateReportScreen
import com.example.weathersnap.presentation.report.CreateReportViewModel
import com.example.weathersnap.presentation.savedreports.SavedReportsScreen
import com.example.weathersnap.presentation.theme.WeatherSnapTheme
import com.example.weathersnap.presentation.weather.WeatherScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WeatherSnapTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = "weather"
                    ) {
                        // ── Weather Screen ─────────────────────────────────
                        composable("weather") {
                            WeatherScreen(
                                // ✅ FIX 1: Yahan 'snapshot: WeatherSnapshot' explicitly define kar diya
                                onNavigateToCreateReport = { snapshot: WeatherSnapshot ->
                                    navController.currentBackStackEntry
                                        ?.savedStateHandle
                                        ?.set("weather_data", snapshot)
                                    navController.navigate("create_report")
                                },
                                onNavigateToReports = {
                                    navController.navigate("saved_reports")
                                }
                            )
                        }

                        // ── Create Report Screen ───────────────────────────
                        composable("create_report") { backStackEntry ->
                            val snapshot = navController.previousBackStackEntry
                                ?.savedStateHandle
                                ?.get<WeatherSnapshot>("weather_data")

                            val viewModel: CreateReportViewModel = hiltViewModel(backStackEntry)

                            if (snapshot != null) {
                                CreateReportScreen(
                                    snapshot = snapshot,
                                    onNavigateToCamera = {
                                        navController.navigate("camera")
                                    },
                                    onSaveSuccess = {
                                        navController.navigate("saved_reports") {
                                            popUpTo("weather")
                                        }
                                    },
                                    viewModel = viewModel
                                )
                            }
                        }

                        // ── Camera Screen ──────────────────────────────────
                        composable("camera") {
                            val reportBackStackEntry = remember(navController) {
                                navController.getBackStackEntry("create_report")
                            }

                            val viewModel: CreateReportViewModel = hiltViewModel(reportBackStackEntry)

                            CameraScreen(
                                onImageCaptured = { path, orig, comp ->
                                    viewModel.onImageCaptured(path, orig, comp)
                                    navController.popBackStack()
                                },
                                onBack = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        // ── Saved Reports Screen ───────────────────────────
                        composable("saved_reports") {
                            SavedReportsScreen(
                                onBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}