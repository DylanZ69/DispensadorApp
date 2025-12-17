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

    private var ultimaHoraGuardada: String? = null

    /* ===================== INIT ===================== */
    init {
        inicializarFirebase()
        escucharModo()
        escucharHorarios()
        escucharGramos()
        escucharMonitoreo()   // 🔥 ESTE ES EL CLAVE PARA EL HISTORIAL
    }

    /* ===================== INIT SEGURO ===================== */
    private fun inicializarFirebase() {

        rtdb.child("config").child("modoAutomatico")
            .get().addOnSuccessListener {
                if (!it.exists()) {
                    rtdb.child("config").child("modoAutomatico").setValue(false)
                }
            }

        rtdb.child("dispensacion").child("gramos")
            .get().addOnSuccessListener {
                if (!it.exists()) {
                    rtdb.child("dispensacion").child("gramos").setValue(20)
                }
            }

        rtdb.child("dispensacion").child("activar")
            .get().addOnSuccessListener {
                if (!it.exists()) {
                    rtdb.child("dispensacion").child("activar").setValue(false)
                }
            }

        rtdb.child("monitoreo").child("ultimaDispensacion")
            .get().addOnSuccessListener {
                if (!it.exists()) {
                    rtdb.child("monitoreo")
                        .child("ultimaDispensacion")
                        .setValue("--")
                }
            }
    }

    /* ===================== MODO ===================== */
    private fun escucharModo() {
        rtdb.child("config").child("modoAutomatico")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    automatico = snapshot.getValue(Boolean::class.java) ?: false
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    fun cambiarModo(value: Boolean) {
        rtdb.child("config").child("modoAutomatico").setValue(value)
    }

    /* ===================== HORARIOS ===================== */
    private fun escucharHorarios() {
        rtdb.child("horarios")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val lista = snapshot.children.mapNotNull { child ->
                        val hora = child.child("hora").getValue(String::class.java)
                        val activo = child.child("activo")
                            .getValue(Boolean::class.java) ?: true

                        if (hora != null) {
                            HorarioItem(
                                id = child.key ?: "",
                                hora = hora,
                                activo = activo
                            )
                        } else null
                    }
                    horarios = lista
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    fun agregarHorario(hora: String, callback: (Boolean) -> Unit) {
        if (hora.isBlank()) {
            callback(false)
            return
        }

        val key = hora.replace(":", "_").replace(" ", "_")

        val data = mapOf(
            "hora" to hora,
            "activo" to true
        )

        rtdb.child("horarios")
            .child(key)
            .setValue(data)
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }
    }

    fun cambiarEstadoHorario(id: String, activo: Boolean) {
        rtdb.child("horarios")
            .child(id)
            .child("activo")
            .setValue(activo)
    }

    /* ===================== MANUAL ===================== */
    fun dispensarManual(gramos: Int) {
        rtdb.child("dispensacion").child("gramos").setValue(gramos)
        rtdb.child("dispensacion").child("activar").setValue(true)
    }

    /* ===================== GRAMOS ===================== */
    private fun escucharGramos() {
        rtdb.child("dispensacion").child("gramos")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    gramosConfigurados =
                        snapshot.getValue(Int::class.java) ?: 0
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    /* ===================== MONITOREO + HISTORIAL ===================== */
    private fun escucharMonitoreo() {
        rtdb.child("monitoreo")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    val hora = snapshot.child("ultimaDispensacion")
                        .getValue(String::class.java) ?: return

                    if (hora == ultimaHoraGuardada) return
                    ultimaHoraGuardada = hora

                    ultimaDispensacion = hora

                    val gramos = snapshot.child("gramos")
                        .getValue(Int::class.java) ?: 0

                    val tipo = snapshot.child("tipo")
                        .getValue(String::class.java) ?: "manual"

                    // 🔵 HISTORIAL OFICIAL (Firestore)
                    firestore.collection("dispensaciones")
                        .add(
                            mapOf(
                                "hora" to hora,
                                "gramos" to gramos,
                                "tipo" to tipo,
                                "timestamp" to System.currentTimeMillis()
                            )
                        )
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }
}
