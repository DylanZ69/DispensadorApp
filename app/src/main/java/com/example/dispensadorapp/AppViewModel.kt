package com.example.dispensadorapp

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

data class HorarioItem(
    val id: String = "",
    val hora: String = "",
    val activo: Boolean = true,
    val timestamp: Long = 0L
)

class AppViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    var automatico by mutableStateOf(true)
        private set

    var horarios by mutableStateOf<List<HorarioItem>>(emptyList())
        private set

    var ultimaDispensacion by mutableStateOf("--")
        private set

    init {
        loadModo()
        loadHorarios()
        loadMonitoreo()
    }

    private fun loadModo() {
        db.collection("config").document("modo")
            .addSnapshotListener { snap, _ ->
                automatico = snap?.getBoolean("automatico") ?: true
            }
    }

    fun cambiarModo(value: Boolean) {
        automatico = value
        db.collection("config").document("modo")
            .set(mapOf("automatico" to value))
    }

    private fun loadHorarios() {
        db.collection("horarios")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, _ ->
                horarios = snap?.documents?.map { doc ->
                    HorarioItem(
                        id = doc.id,
                        hora = doc.getString("hora") ?: "",
                        activo = doc.getBoolean("activo") ?: true,
                        timestamp = doc.getLong("timestamp") ?: 0L
                    )
                } ?: emptyList()
            }
    }

    fun agregarHorario(hora: String, callback: (Boolean) -> Unit) {
        if (hora.isBlank()) {
            callback(false)
            return
        }

        val data = mapOf(
            "hora" to hora,
            "activo" to true,
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("horarios")
            .add(data)
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }
    }

    fun cambiarEstadoHorario(id: String, activo: Boolean) {
        db.collection("horarios").document(id)
            .update("activo", activo)
    }


    private fun loadMonitoreo() {
        db.collection("monitoreo").document("ultima")
            .addSnapshotListener { snap, _ ->
                ultimaDispensacion = snap?.getString("hora") ?: "--"
            }
    }

    fun actualizarDispensacion(hora: String) {
        db.collection("monitoreo").document("ultima")
            .set(mapOf("hora" to hora))
    }
}
