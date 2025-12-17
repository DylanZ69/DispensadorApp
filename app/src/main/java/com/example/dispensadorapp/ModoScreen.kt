package com.example.dispensadorapp

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun ModoScreen(navController: NavController, vm: AppViewModel) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "Modo de Alimentación",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {

            Switch(
                checked = vm.automatico,
                onCheckedChange = { nuevoValor ->
                    vm.cambiarModo(nuevoValor)
                }
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = if (vm.automatico) "Automático" else "Manual",
                style = MaterialTheme.typography.bodyLarge
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = if (vm.automatico)
                "El dispensador funcionará según los horarios configurados."
            else
                "El dispensador solo funcionará cuando presiones el botón manual.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
