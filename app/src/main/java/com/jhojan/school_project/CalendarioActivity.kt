package com.jhojan.school_project

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class CalendarioActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore

    // Header y Footer
    private lateinit var teacherHeader: TeacherHeader
    private lateinit var bottomNav: TeacherBottomNavigationView

    // Views
    private lateinit var calendarView: CalendarView
    private lateinit var tvFechaSeleccionada: TextView
    private lateinit var layoutEventos: LinearLayout
    private lateinit var scrollEventos: ScrollView

    // Datos
    private var profesorId: String = ""
    private val eventosYTareas = mutableMapOf<String, MutableList<EventoTarea>>()
    private var fechaSeleccionada: String = ""

    data class EventoTarea(
        val id: String,
        val tipo: String, // "evento" o "tarea"
        val titulo: String,
        val descripcion: String,
        val fecha: Date,
        val estudiante: String? = null,
        val materia: String? = null
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendario)

        // Inicializar Firestore
        db = FirebaseFirestore.getInstance()

        // Obtener el ID del profesor desde SharedPreferences
        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        profesorId = prefs.getString("profesor_id", "") ?: ""

        // Si no hay profesor guardado, usar uno por defecto
        if (profesorId.isEmpty()) {
            profesorId = "ZW6kOK1PaGVcteR4N9mzSQlcxjd2"
        }

        // Inicializar vistas
        initViews()

        // Configurar Header y Footer
        setupHeaderFooter()

        // Configurar listeners
        setupListeners()

        // Cargar datos
        cargarEventosYTareas()

        // Establecer fecha actual por defecto
        val calendar = Calendar.getInstance()
        fechaSeleccionada = formatearFecha(calendar.time)
        actualizarTituloFecha(calendar.time)
    }

    private fun initViews() {
        teacherHeader = findViewById(R.id.teacherHeader)
        bottomNav = findViewById(R.id.bottomNav)
        calendarView = findViewById(R.id.calendarView)
        tvFechaSeleccionada = findViewById(R.id.tvFechaSeleccionada)
        layoutEventos = findViewById(R.id.layoutEventos)
        scrollEventos = findViewById(R.id.scrollEventos)
    }

    private fun setupHeaderFooter() {
        // Configurar Header
        teacherHeader.loadTeacherData(profesorId)
        teacherHeader.setOnBackClickListener {
            finish()
        }

        // Configurar Footer
        bottomNav.setActiveItem(TeacherBottomNavigationView.NavigationItem.TAREAS)
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

        // Cargar eventos
        db.collection("mensajes_eventos")
            .whereEqualTo("tipo", "evento")
            .whereEqualTo("profesor", profesorId)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val descripcion = document.getString("descripcion") ?: ""
                    val eventDate = document.getTimestamp("eventDate")
                    val estudianteId = document.getString("estudiante")

                    if (eventDate != null) {
                        val fecha = eventDate.toDate()
                        val fechaKey = formatearFecha(fecha)

                        val evento = EventoTarea(
                            id = document.id,
                            tipo = "evento",
                            titulo = "Evento",
                            descripcion = descripcion,
                            fecha = fecha,
                            estudiante = estudianteId
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
                cargarTareas()
            }
    }

    private fun cargarTareas() {
        db.collection("tareas")
            .whereEqualTo("profesor", profesorId)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val titulo = document.getString("titulo") ?: "Sin título"
                    val descripcion = document.getString("descripcion") ?: ""
                    val fechaEntrega = document.getTimestamp("fecha_entrega")
                    val materia = document.getString("materia")

                    if (fechaEntrega != null) {
                        val fecha = fechaEntrega.toDate()
                        val fechaKey = formatearFecha(fecha)

                        val tarea = EventoTarea(
                            id = document.id,
                            tipo = "tarea",
                            titulo = titulo,
                            descripcion = descripcion,
                            fecha = fecha,
                            materia = materia
                        )

                        if (!eventosYTareas.containsKey(fechaKey)) {
                            eventosYTareas[fechaKey] = mutableListOf()
                        }
                        eventosYTareas[fechaKey]?.add(tarea)
                    }
                }

                // Mostrar eventos de la fecha actual
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
        val sdf = SimpleDateFormat("EEEE, d 'de' MMMM 'de' yyyy", Locale("es", "ES"))
        tvFechaSeleccionada.text = sdf.format(fecha).capitalize()
    }

    private fun mostrarEventosDeFecha(fecha: String) {
        layoutEventos.removeAllViews()

        val eventos = eventosYTareas[fecha]

        if (eventos.isNullOrEmpty()) {
            val tvNoEventos = TextView(this)
            tvNoEventos.text = "No hay eventos ni tareas para este día"
            tvNoEventos.textSize = 16f
            tvNoEventos.setTextColor(Color.GRAY)
            tvNoEventos.setPadding(16, 32, 16, 16)
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
            android.R.layout.simple_list_item_2,
            layoutEventos,
            false
        )

        val tvTitulo = itemView.findViewById<TextView>(android.R.id.text1)
        val tvDetalle = itemView.findViewById<TextView>(android.R.id.text2)

        // Configurar según el tipo
        if (evento.tipo == "evento") {
            tvTitulo.text = "📅 ${evento.titulo}"
            tvTitulo.setTextColor(ContextCompat.getColor(this, android.R.color.holo_blue_dark))
            tvDetalle.text = evento.descripcion
        } else {
            tvTitulo.text = "📝 ${evento.titulo}"
            tvTitulo.setTextColor(ContextCompat.getColor(this, android.R.color.holo_orange_dark))
            val horario = SimpleDateFormat("HH:mm", Locale.getDefault()).format(evento.fecha)
            tvDetalle.text = "${evento.descripcion}\n${evento.materia ?: ""}"
        }

        tvTitulo.textSize = 18f
        tvDetalle.textSize = 14f
        tvDetalle.setTextColor(Color.DKGRAY)

        // Padding y margen
        itemView.setPadding(24, 16, 24, 16)
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 0, 0, 16)
        itemView.layoutParams = params

        // Fondo con borde
        itemView.setBackgroundResource(android.R.drawable.dialog_holo_light_frame)

        return itemView
    }
}