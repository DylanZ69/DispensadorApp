package com.example.dispensadorapp

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistorialScreen(navController: NavController) {

    val db = FirebaseFirestore.getInstance()
    var historial by remember { mutableStateOf<List<DispItem>>(emptyList()) }

    LaunchedEffect(true) {
        db.collection("dispensaciones")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->

                if (snapshot != null) {
                    historial = snapshot.documents.mapNotNull { doc ->
                        val gramos = doc.getLong("gramos")
                        val timestamp = doc.getLong("timestamp")

                        if (gramos != null && timestamp != null) {
                            DispItem(
                                gramos = gramos.toInt(),
                                timestamp = timestamp
                            )
                        } else null
                    }
                }
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {

        Text(
            text = "Historial de Dispensas",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (historial.isEmpty()) {
            Text("No hay dispensaciones registradas.")
        } else {
            historial.forEach { item ->

                val hora = SimpleDateFormat(
                    "hh:mm a",
                    Locale.getDefault()
                ).format(Date(item.timestamp))

                Text("• ${item.gramos} g   |   $hora")
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

data class DispItem(
    val gramos: Int,
    val timestamp: Long
)
