package com.jhojan.school_project

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class CalendarioAcudienteActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    // Header y Footer
    private lateinit var parentHeader: ParentHeader
    private lateinit var bottomNav: ParentBottomNavigationView

    // Views
    private lateinit var calendarView: CalendarView
    private lateinit var tvFechaSeleccionada: TextView
    private lateinit var layoutEventos: LinearLayout
    private lateinit var scrollEventos: ScrollView

    // Datos
    private var estudianteId: String = ""
    private val eventosYTareas = mutableMapOf<String, MutableList<EventoTarea>>()
    private var fechaSeleccionada: String = ""

    data class EventoTarea(
        val id: String,
        val tipo: String, // "evento" o "tarea"
        val titulo: String,
        val descripcion: String,
        val fecha: Date,
        val materia: String? = null,
        val estado: String? = null
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendario_acudiente)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        initViews()

        // Recuperar estudiante si ya existía en SharedPreferences
        estudianteId = getSharedPreferences("user_prefs", MODE_PRIVATE)
            .getString("estudiante_id", "") ?: ""


        setupListeners()
        setupHeaderFooter()

        val acudienteId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""

        db.collection("users")
            .document(acudienteId)
            .get()
            .addOnSuccessListener { acudienteDoc ->

                if (acudienteDoc.exists()) {

                    estudianteId = acudienteDoc.getString("estudiante_id") ?: ""

                    if (estudianteId.isEmpty()) {
                        Toast.makeText(this, "No se encontró estudiante asignado", Toast.LENGTH_LONG).show()
                        return@addOnSuccessListener
                    }

                    // Guardar estudiante
                    getSharedPreferences("user_prefs", MODE_PRIVATE)
                        .edit().putString("estudiante_id", estudianteId).apply()

                    val acudienteId = auth.currentUser?.uid ?: ""
                    // AHORA SÍ cargar header
                    parentHeader.loadParentData(acudienteId)

                    // Cargar eventos y tareas
                    cargarEventosYTareas()

                    // Fecha actual
                    val calendar = Calendar.getInstance()
                    fechaSeleccionada = formatearFecha(calendar.time)
                    actualizarTituloFecha(calendar.time)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error obteniendo estudiante_id", Toast.LENGTH_LONG).show()
            }

    }



    private fun initViews() {
        parentHeader = findViewById(R.id.parentHeader)
        bottomNav = findViewById(R.id.bottomNav)
        calendarView = findViewById(R.id.calendarView)
        tvFechaSeleccionada = findViewById(R.id.tvFechaSeleccionada)
        layoutEventos = findViewById(R.id.layoutEventos)
        scrollEventos = findViewById(R.id.scrollEventos)
    }

    private fun setupHeaderFooter() {

        // SOLO cargar datos si estudianteId NO está vacío
        if (estudianteId.isNotEmpty()) {
            parentHeader.loadParentData(estudianteId)
        }

        parentHeader.setOnBackClickListener { finish() }

        bottomNav.setActiveItem(ParentBottomNavigationView.NavigationItem.CALENDARIO)
    }


    private fun setupListeners() {
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth)
            fechaSeleccionada = formatearFecha(calendar.time)
            actualizarTituloFecha(calendar.time)
            mostrarEventosDeFecha(fechaSeleccionada)
        }
    }

    private fun cargarEventosYTareas() {
        eventosYTareas.clear()

        // Cargar eventos para el estudiante
        db.collection("mensajes_eventos")
            .whereEqualTo("tipo", "evento")
            .whereEqualTo("estudiante", estudianteId)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val descripcion = document.getString("descripcion") ?: ""
                    val eventDate = document.getTimestamp("eventDate")

                    if (eventDate != null) {
                        val fecha = eventDate.toDate()
                        val fechaKey = formatearFecha(fecha)

                        val evento = EventoTarea(
                            id = document.id,
                            tipo = "evento",
                            titulo = "Evento Escolar",
                            descripcion = descripcion,
                            fecha = fecha
                        )

                        if (!eventosYTareas.containsKey(fechaKey)) {
                            eventosYTareas[fechaKey] = mutableListOf()
                        }
                        eventosYTareas[fechaKey]?.add(evento)
                    }
                }

                // Después de cargar eventos, cargar tareas
                cargarTareas()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al cargar eventos: ${e.message}", Toast.LENGTH_SHORT).show()

            }
    }

    private fun cargarTareas() {
        db.collection("tareas")
            .whereEqualTo("estudiante", estudianteId) // <-- CAMBIO AQUÍ
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val titulo = document.getString("titulo") ?: "Sin título"
                    val descripcion = document.getString("descripcion") ?: ""
                    val fechaEntrega = document.getTimestamp("fecha_entrega")
                    val materia = document.getString("materia")
                    val estado = document.getString("estado") ?: "pendiente"

                    if (fechaEntrega != null) {
                        val fecha = fechaEntrega.toDate()
                        val fechaKey = formatearFecha(fecha)

                        val tarea = EventoTarea(
                            id = document.id,
                            tipo = "tarea",
                            titulo = titulo,
                            descripcion = descripcion,
                            fecha = fecha,
                            materia = materia,
                            estado = estado
                        )

                        if (!eventosYTareas.containsKey(fechaKey)) {
                            eventosYTareas[fechaKey] = mutableListOf()
                        }
                        eventosYTareas[fechaKey]?.add(tarea)
                    }
                }

                if (fechaSeleccionada.isEmpty()) {
                    fechaSeleccionada = formatearFecha(Date())
                }

                mostrarEventosDeFecha(fechaSeleccionada)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al cargar tareas: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }



    private fun formatearFecha(fecha: Date): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(fecha)
    }

    private fun actualizarTituloFecha(fecha: Date) {
        val sdf = SimpleDateFormat("MMMM d, yyyy", Locale("en", "US"))
        tvFechaSeleccionada.text = sdf.format(fecha)
    }

    private fun mostrarEventosDeFecha(fecha: String) {
        layoutEventos.removeAllViews()

        val eventos = eventosYTareas[fecha]

        if (eventos.isNullOrEmpty()) {
            val tvNoEventos = TextView(this)
            tvNoEventos.text = "No hay actividades programadas para este día"
            tvNoEventos.textSize = 16f
            tvNoEventos.setTextColor(Color.parseColor("#757575"))
            tvNoEventos.gravity = android.view.Gravity.CENTER
            tvNoEventos.setPadding(16, 48, 16, 48)
            layoutEventos.addView(tvNoEventos)
            return
        }

        // Ordenar por hora
        eventos.sortBy { it.fecha }

        for (evento in eventos) {
            val itemView = crearVistaEvento(evento)
            layoutEventos.addView(itemView)
        }
    }

    private fun crearVistaEvento(evento: EventoTarea): View {
        val itemView = LayoutInflater.from(this).inflate(
            R.layout.item_calendar_event_a,
            layoutEventos,
            false
        )

        val iconEvento = itemView.findViewById<ImageView>(R.id.iconEvento)
        val tvTituloEvento = itemView.findViewById<TextView>(R.id.tvTituloEvento)
        val tvTipoEvento = itemView.findViewById<TextView>(R.id.tvTipoEvento)
        val tvDetalleEvento = itemView.findViewById<TextView>(R.id.tvDetalleEvento)

        // Configurar según el tipo
        if (evento.tipo == "evento") {
            iconEvento.setImageResource(R.drawable.ic_calendar)
            iconEvento.setColorFilter(ContextCompat.getColor(this, R.color.blue_primary))
            tvTituloEvento.text = evento.titulo
            tvTipoEvento.text = "School Event"
            tvTipoEvento.setTextColor(ContextCompat.getColor(this, R.color.blue_primary))
            tvDetalleEvento.text = evento.descripcion
        } else {
            iconEvento.setImageResource(R.drawable.ic_tasks)

            when (evento.estado) {
                "completada" -> {
                    iconEvento.setColorFilter(ContextCompat.getColor(this, R.color.green_success))
                    tvTipoEvento.setTextColor(ContextCompat.getColor(this, R.color.green_success))
                }
                else -> {
                    iconEvento.setColorFilter(ContextCompat.getColor(this, R.color.orange_accent))
                    tvTipoEvento.setTextColor(ContextCompat.getColor(this, R.color.orange_accent))
                }
            }

            tvTituloEvento.text = evento.titulo
            tvTipoEvento.text = evento.materia ?: "Tarea"

            val horario = SimpleDateFormat("h:mm a", Locale.getDefault()).format(evento.fecha)
            tvDetalleEvento.text = if (evento.descripcion.isNotEmpty()) {
                evento.descripcion
            } else {
                "Entrega: $horario"
            }
        }

        return itemView
    }
}