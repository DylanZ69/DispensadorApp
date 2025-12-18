package com.example.dispensadorapp

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore

data class HorarioItem(
    val id: String = "",
    val hora: String = "",
    val activo: Boolean = true
)

class AppViewModel : ViewModel() {

    /* ===================== FIREBASE ===================== */
    private val rtdb = FirebaseDatabase.getInstance().reference
    private val firestore = FirebaseFirestore.getInstance()

    /* ===================== STATE ===================== */
    var automatico by mutableStateOf(false)
        private set

    var horarios by mutableStateOf<List<HorarioItem>>(emptyList())
        private set

    var ultimaDispensacion by mutableStateOf("--")
        private set

    var gramosConfigurados by mutableStateOf(0)
        private set

    // 🔔 ALERTA DE NIVEL BAJO
    var alertaNivelBajo by mutableStateOf(false)
        private set

    // 🔐 evitar duplicados en historial
    private var ultimoEventoId: String? = null

    /* ===================== INIT ===================== */
    init {
        inicializarFirebase()
        escucharModo()
        escucharHorarios()
        escucharGramos()
        escucharMonitoreo()
    }

    /* ===================== INIT SEGURO ===================== */
    private fun inicializarFirebase() {

        rtdb.child("config/modoAutomatico").get().addOnSuccessListener {
            if (!it.exists()) rtdb.child("config/modoAutomatico").setValue(false)
        }

        rtdb.child("dispensacion/gramos").get().addOnSuccessListener {
            if (!it.exists()) rtdb.child("dispensacion/gramos").setValue(20)
        }

        rtdb.child("dispensacion/activar").get().addOnSuccessListener {
            if (!it.exists()) rtdb.child("dispensacion/activar").setValue(false)
        }
    }

    /* ===================== MODO ===================== */
    private fun escucharModo() {
        rtdb.child("config/modoAutomatico")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    automatico = snapshot.getValue(Boolean::class.java) ?: false
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    fun cambiarModo(value: Boolean) {
        rtdb.child("config/modoAutomatico").setValue(value)
    }

    /* ===================== HORARIOS ===================== */
    private fun escucharHorarios() {
        rtdb.child("horarios")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    horarios = snapshot.children.mapNotNull { child ->
                        val hora = child.child("hora").getValue(String::class.java)
                        val activo = child.child("activo")
                            .getValue(Boolean::class.java) ?: true

                        hora?.let {
                            HorarioItem(
                                id = child.key ?: "",
                                hora = it,
                                activo = activo
                            )
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    fun agregarHorario(hora: String, callback: (Boolean) -> Unit) {
        if (hora.isBlank()) return callback(false)

        val key = hora.replace(":", "_").replace(" ", "_")
        val data = mapOf("hora" to hora, "activo" to true)

        rtdb.child("horarios/$key")
            .setValue(data)
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }
    }

    fun cambiarEstadoHorario(id: String, activo: Boolean) {
        rtdb.child("horarios/$id/activo").setValue(activo)
    }

    /* ===================== MANUAL ===================== */
    fun dispensarManual(gramos: Int) {
        rtdb.child("dispensacion/gramos").setValue(gramos)
        rtdb.child("dispensacion/activar").setValue(true)
    }

    /* ===================== GRAMOS ===================== */
    private fun escucharGramos() {
        rtdb.child("dispensacion/gramos")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    gramosConfigurados = snapshot.getValue(Int::class.java) ?: 0
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    /* ===================== MONITOREO + HISTORIAL + ALERTA ===================== */
    private fun escucharMonitoreo() {
        rtdb.child("monitoreo")
            .addValueEventListener(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {

                    val hora = snapshot.child("ultimaDispensacion")
                        .getValue(String::class.java) ?: return

                    val gramosDisp = snapshot.child("gramosDispensados")
                        .getValue(Int::class.java) ?: return

                    val tipo = snapshot.child("tipo")
                        .getValue(String::class.java) ?: "manual"

                    val nivel = snapshot.child("nivelEstimado")
                        .getValue(Int::class.java) ?: return

                    // 🔐 evitar duplicados en historial
                    val eventoId = "$hora-$tipo"
                    if (eventoId == ultimoEventoId) return
                    ultimoEventoId = eventoId

                    ultimaDispensacion = hora

                    // 🔔 leer nivel mínimo de alerta
                    rtdb.child("alertas/nivelMinimo")
                        .get()
                        .addOnSuccessListener { snap ->
                            val minimo = snap.getValue(Int::class.java) ?: 0
                            alertaNivelBajo = nivel <= minimo
                        }

                    // guardar historial
                    firestore.collection("dispensaciones")
                        .add(
                            mapOf(
                                "hora" to hora,
                                "gramos" to gramosDisp,
                                "tipo" to tipo,
                                "timestamp" to System.currentTimeMillis()
                            )
                        )
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }
}
