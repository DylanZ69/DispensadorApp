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
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MonitoreoScreen(navController: NavController) {

    val db = FirebaseFirestore.getInstance()

    var gramos by remember { mutableStateOf("") }
    var ultimo by remember { mutableStateOf("N/A") }
    var mensaje by remember { mutableStateOf("") }

    // Cargar última dispensación en tiempo real
    LaunchedEffect(true) {
        db.collection("dispensaciones")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(1)
            .addSnapshotListener { snap, _ ->
                if (snap != null && !snap.isEmpty) {
                    val doc = snap.documents.first()
                    val g = doc.getLong("gramos")
                    val t = doc.getLong("timestamp")

                    if (g != null && t != null) {
                        val hora = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(t))
                        ultimo = "$g g   |   $hora"
                    }
                }
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFC400))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text("Dispensar Gramos", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(20.dp))

        OutlinedTextField(
            value = gramos,
            onValueChange = { gramos = it },
            label = { Text("Cantidad en gramos") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                if (gramos.isNotBlank()) {
                    val cantidad = gramos.toIntOrNull()

                    if (cantidad != null) {
                        val data = mapOf(
                            "gramos" to cantidad,
                            "timestamp" to System.currentTimeMillis()
                        )

                        db.collection("dispensaciones")
                            .add(data)
                            .addOnSuccessListener {
                                mensaje = "Dispensación guardada"
                                gramos = ""
                            }
                            .addOnFailureListener {
                                mensaje = "Error al guardar"
                            }
                    } else {
                        mensaje = "Ingresa un número válido"
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Dispensar")
        }

        Spacer(Modifier.height(20.dp))
        Text("Última dispensación:", style = MaterialTheme.typography.titleMedium)
        Text(ultimo, style = MaterialTheme.typography.titleLarge)

        if (mensaje.isNotEmpty()) {
            Spacer(Modifier.height(20.dp))
            Text(mensaje, color = Color.DarkGray)
        }
    }
}
