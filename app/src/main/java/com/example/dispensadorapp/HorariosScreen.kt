package com.example.dispensadorapp

import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.util.*

@Composable
fun HorariosScreen(navController: NavController, vm: AppViewModel) {

    var horaSeleccionada by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf<String?>(null) }

    val calendar = Calendar.getInstance()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {

        Text(
            text = "Registrar Horarios",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                val hora = calendar.get(Calendar.HOUR_OF_DAY)
                val minuto = calendar.get(Calendar.MINUTE)

                TimePickerDialog(
                    navController.context,
                    { _, newHour, newMinute ->

                        // ✅ FORMATO 24 HORAS (HH:mm)
                        horaSeleccionada =
                            String.format("%02d:%02d", newHour, newMinute)

                    },
                    hora,
                    minuto,
                    true // 👈 true = selector en formato 24h
                ).show()
            }
        ) {
            Text(
                if (horaSeleccionada.isEmpty())
                    "Seleccionar hora"
                else
                    horaSeleccionada
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                if (horaSeleccionada.isNotBlank()) {
                    vm.agregarHorario(horaSeleccionada) { ok ->
                        mensaje = if (ok) {
                            horaSeleccionada = ""
                            "Horario guardado correctamente"
                        } else {
                            "Error al guardar el horario"
                        }
                    }
                }
            }
        ) {
            Text("Guardar horario")
        }

        mensaje?.let {
            Spacer(modifier = Modifier.height(12.dp))
            Text(it, color = MaterialTheme.colorScheme.primary)
        }

        Spacer(modifier = Modifier.height(26.dp))

        Text(
            text = "Horarios configurados",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn {
            items(vm.horarios) { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(item.hora)

                    Switch(
                        checked = item.activo,
                        onCheckedChange = { nuevoEstado ->
                            vm.cambiarEstadoHorario(
                                id = item.id,
                                activo = nuevoEstado
                            )
                        }
                    )
                }
            }
        }
    }
}
