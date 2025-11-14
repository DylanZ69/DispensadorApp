package com.example.dispensadorapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

data class BottomNavItem(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val route: String
)

@Composable
fun BottomBar(navController: NavController) {

    val items = listOf(
        BottomNavItem("Inicio", Icons.Default.Home, "home"),
        BottomNavItem("On/Off", Icons.Default.Settings, "modo"),
        BottomNavItem("Horarios", Icons.Default.Schedule, "horarios"),
        BottomNavItem("Gramos", Icons.Default.Visibility, "monitoreo"),
        BottomNavItem("Historial", Icons.Default.History, "historial"),
    )

    NavigationBar(
        containerColor = Color(0xFF2B2424)
    ) {
        val navBackStackEntry = navController.currentBackStackEntryAsState()

        items.forEach { item ->
            val selected = navBackStackEntry.value?.destination?.route == item.route

            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(item.route) {
                        launchSingleTop = true
                    }
                },
                icon = {
                    if (selected) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = androidx.compose.ui.Alignment.Center
                        ) {
                            Icon(
                                item.icon,
                                contentDescription = item.label,
                                tint = Color(0xFF2B2424)
                            )
                        }
                    } else {
                        Icon(
                            item.icon,
                            contentDescription = item.label,
                            tint = Color(0xFFFFC400)
                        )
                    }
                },
                label = {
                    Text(
                        item.label,
                        color = if (selected) Color.White else Color(0xFFFFC400)
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}
