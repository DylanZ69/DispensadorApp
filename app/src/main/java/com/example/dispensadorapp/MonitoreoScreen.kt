package com.example.dispensadorapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun MonitoreoScreen(navController: NavController, vm: AppViewModel) {

    var gramosInput by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFC400))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Dispensar Gramos",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(Modifier.height(20.dp))

        OutlinedTextField(
            value = gramosInput,
            onValueChange = { gramosInput = it },
            label = { Text("Cantidad en gramos") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !vm.automatico
        )

        Spacer(Modifier.height(16.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = !vm.automatico,
            onClick = {
                val cantidad = gramosInput.toIntOrNull()
                if (cantidad != null && cantidad > 0) {
                    vm.dispensarManual(cantidad)
                    mensaje = "Dispensando..."
                    gramosInput = ""
                } else {
                    mensaje = "Ingresa un número válido"
                }
            }
        ) {
            Text("Dispensar")
        }

        if (vm.automatico) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Cambia a modo Manual para dispensar",
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(Modifier.height(24.dp))

        Text(
            text = "Última dispensación",
            style = MaterialTheme.typography.titleMedium
        )
        if (vm.alertaNivelBajo) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "⚠️ Nivel bajo de alimento",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyLarge
            )
        }


        Text(
            text = vm.ultimaDispensacion,
            style = MaterialTheme.typography.titleLarge
        )

        if (mensaje.isNotEmpty()) {
            Spacer(Modifier.height(20.dp))
            Text(
                text = mensaje,
                color = Color.DarkGray
            )
        }
    }
}
