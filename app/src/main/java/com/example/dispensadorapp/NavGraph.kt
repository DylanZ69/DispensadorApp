package com.example.dispensadorapp

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    val vm: AppViewModel = viewModel()

    val screenTitles = mapOf(
        "home" to "Panel Principal",
        "monitoreo" to "Monitoreo",
        "modo" to "Modo",
        "horarios" to "Horarios",
        "historial" to "Historial"
    )

    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route

    Scaffold(
        topBar = {
            if (currentRoute != "login") {
                AppTopBar(title = screenTitles[currentRoute] ?: "")
            }
        },
        bottomBar = {
            if (currentRoute != "login") {
                BottomBar(navController)
            }
        }
    ) { paddingValues ->

        NavHost(
            navController = navController,
            startDestination = "login",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("login") {
                LoginScreen(navController)
            }
            composable("home") {
                HomeScreen(navController)
            }
            composable("monitoreo") {
                MonitoreoScreen(navController)
            }
            composable("modo") {
                ModoScreen(navController, vm)
            }
            composable("horarios") {
                HorariosScreen(navController, vm)
            }
            composable("historial") {
                HistorialScreen(navController)
            }
        }
    }
}
