package com.brewandbean.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import com.brewandbean.app.ui.admin.HomeScreen
import com.brewandbean.app.ui.admin.BaristaScreen
import com.brewandbean.app.ui.admin.AdminPanelScreen
import com.brewandbean.app.ui.admin.AdminViewModel
import com.brewandbean.app.ui.theme.BrewAndBeanTheme

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        com.brewandbean.app.util.LanguageManager.init(applicationContext)
        com.brewandbean.app.util.NetworkMonitor.init(applicationContext)
        setContent {
            BrewAndBeanTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val isConnected by com.brewandbean.app.util.NetworkMonitor.isConnected.collectAsState()
                    if (!isConnected) {
                        com.brewandbean.app.ui.admin.NoInternetScreen()
                    } else {
                        val navController = rememberNavController()
                        val adminViewModel = hiltViewModel<AdminViewModel>()
                        
                        NavHost(navController = navController, startDestination = "home") {
                            composable("home") {
                                HomeScreen(
                                    onNavigateToBarista = { navController.navigate("barista") },
                                    onNavigateToAdmin = { navController.navigate("admin") }
                                )
                            }
                            composable("barista") {
                                BaristaScreen(
                                    viewModel = adminViewModel,
                                    onBack = { navController.popBackStack() }
                                )
                            }
                            composable("admin") {
                                AdminPanelScreen(
                                    viewModel = adminViewModel,
                                    onBack = { navController.popBackStack() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
