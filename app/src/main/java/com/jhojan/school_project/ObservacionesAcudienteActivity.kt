package com.jhojan.school_project

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*

class ObservacionesAcudienteActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    // Header y Footer
    private lateinit var parentHeader: ParentHeader
    private lateinit var bottomNav: ParentBottomNavigationView

    // Views
    private lateinit var chipLeve: TextView
    private lateinit var chipMedia: TextView
    private lateinit var chipGrave: TextView
    private lateinit var chipTodas: TextView
    private lateinit var layoutObservaciones: LinearLayout
    private lateinit var scrollObservaciones: ScrollView
    private lateinit var tvNoObservaciones: LinearLayout
    private lateinit var tvTotalObservaciones: TextView

    // Datos
    private var estudianteId: String = ""
    private val observacionesList = mutableListOf<ObservacionData>()
    private var filtroActual: String = "todas" // todas, leve, media, grave

    data class ObservacionData(
        val id: String,
        val descripcion: String,
        val fecha: Date,
        val profesorId: String,
        var profesorNombre: String,
        val tipoFalta: String
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_observaciones_acudiente)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        initViews()
        setupHeaderFooter()
        setupFiltros()

        // Recuperar estudiante
        estudianteId = getSharedPreferences("user_prefs", MODE_PRIVATE)
            .getString("estudiante_id", "") ?: ""

        val acudienteId = auth.currentUser?.uid ?: ""

        Log.d("Observaciones", "Estudiante ID inicial: $estudianteId")
        Log.d("Observaciones", "Acudiente ID: $acudienteId")

        if (estudianteId.isEmpty()) {
            Log.d("Observaciones", "Buscando estudiante_id desde users...")
            db.collection("users")
                .document(acudienteId)
                .get()
                .addOnSuccessListener { acudienteDoc ->
                    if (acudienteDoc.exists()) {
                        estudianteId = acudienteDoc.getString("estudiante_id") ?: ""
                        Log.d("Observaciones", "Estudiante ID encontrado: $estudianteId")

                        if (estudianteId.isEmpty()) {
                            Toast.makeText(this, "No se encontró estudiante asignado", Toast.LENGTH_LONG).show()
                            mostrarSinObservaciones()
                            return@addOnSuccessListener
                        }

                        getSharedPreferences("user_prefs", MODE_PRIVATE)
                            .edit().putString("estudiante_id", estudianteId).apply()

                        parentHeader.loadParentData(acudienteId)
                        cargarObservaciones()
                    } else {
                        Log.e("Observaciones", "Documento de acudiente no existe")
                        Toast.makeText(this, "Error: usuario no encontrado", Toast.LENGTH_LONG).show()
                        mostrarSinObservaciones()
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("Observaciones", "Error obteniendo estudiante", e)
                    Toast.makeText(this, "Error obteniendo estudiante: ${e.message}", Toast.LENGTH_LONG).show()
                    mostrarSinObservaciones()
                }
        } else {
            parentHeader.loadParentData(acudienteId)
            cargarObservaciones()
        }
    }

    private fun initViews() {
        parentHeader = findViewById(R.id.parentHeader)
        bottomNav = findViewById(R.id.bottomNav)
        chipLeve = findViewById(R.id.chipLeve)
        chipMedia = findViewById(R.id.chipMedia)
        chipGrave = findViewById(R.id.chipGrave)
        chipTodas = findViewById(R.id.chipTodas)
        layoutObservaciones = findViewById(R.id.layoutObservaciones)
        scrollObservaciones = findViewById(R.id.scrollObservaciones)
        tvNoObservaciones = findViewById(R.id.layoutNoObservaciones)
        tvTotalObservaciones = findViewById(R.id.tvTotalObservaciones)
    }

    private fun setupHeaderFooter() {
        parentHeader.setOnBackClickListener { finish() }
        bottomNav.setActiveItem(ParentBottomNavigationView.NavigationItem.TAREAS)
    }

    private fun setupFiltros() {
        chipTodas.setOnClickListener {
            filtroActual = "todas"
            actualizarChipsUI()
            filtrarObservaciones()
        }

        chipLeve.setOnClickListener {
            filtroActual = "leve"
            actualizarChipsUI()
            filtrarObservaciones()
        }

        chipMedia.setOnClickListener {
            filtroActual = "media"
            actualizarChipsUI()
            filtrarObservaciones()
        }

        chipGrave.setOnClickListener {
            filtroActual = "grave"
            actualizarChipsUI()
            filtrarObservaciones()
        }

        actualizarChipsUI()
    }

    private fun actualizarChipsUI() {
        // Reset todos
        resetChip(chipTodas)
        resetChip(chipLeve)
        resetChip(chipMedia)
        resetChip(chipGrave)

        // Activar el seleccionado
        when (filtroActual) {
            "todas" -> activarChip(chipTodas, R.color.blue_primary)
            "leve" -> activarChip(chipLeve, R.color.yellow_warning)
            "media" -> activarChip(chipMedia, R.color.orange_accent)
            "grave" -> activarChip(chipGrave, R.color.red_error)
        }
    }

    private fun resetChip(chip: TextView) {
        chip.setBackgroundResource(R.drawable.chip_background_inactive)
        chip.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))
    }

    private fun activarChip(chip: TextView, colorRes: Int) {
        chip.setBackgroundResource(R.drawable.chip_background_active)
        chip.setTextColor(ContextCompat.getColor(this, colorRes))
    }

    private fun cargarObservaciones() {
        Log.d("Observaciones", "Iniciando carga de observaciones para estudiante: $estudianteId")
        observacionesList.clear()

        db.collection("observaciones")
            .whereEqualTo("estudiante", estudianteId)
            .get()
            .addOnSuccessListener { snapshot ->
                Log.d("Observaciones", "Query exitosa. Documentos encontrados: ${snapshot.size()}")

                if (snapshot.isEmpty) {
                    Log.d("Observaciones", "No hay observaciones para este estudiante")
                    mostrarSinObservaciones()
                    return@addOnSuccessListener
                }

                for (document in snapshot) {
                    Log.d("Observaciones", "Procesando documento: ${document.id}")
                    Log.d("Observaciones", "Datos: ${document.data}")

                    val descripcion = document.getString("descripcion") ?: ""
                    val fechaTimestamp = document.getTimestamp("fecha")
                    val fecha = fechaTimestamp?.toDate() ?: Date()
                    val profesorId = document.getString("profesor") ?: ""
                    val tipoFalta = document.getString("tipo_falta") ?: "leve"

                    Log.d("Observaciones", "Tipo falta: $tipoFalta, Profesor: $profesorId")

                    observacionesList.add(
                        ObservacionData(
                            id = document.id,
                            descripcion = descripcion,
                            fecha = fecha,
                            profesorId = profesorId,
                            profesorNombre = "",
                            tipoFalta = tipoFalta
                        )
                    )
                }

                Log.d("Observaciones", "Total observaciones agregadas: ${observacionesList.size}")

                // Ordenar por fecha descendente
                observacionesList.sortByDescending { it.fecha }

                cargarNombresProfesores()
            }
            .addOnFailureListener { e ->
                Log.e("Observaciones", "Error al cargar observaciones", e)
                Toast.makeText(this, "Error al cargar observaciones: ${e.message}", Toast.LENGTH_SHORT).show()
                mostrarSinObservaciones()
            }
    }

    private fun cargarNombresProfesores() {
        val profesoresIds = observacionesList.map { it.profesorId }.distinct().filter { it.isNotEmpty() }

        Log.d("Observaciones", "Cargando nombres de profesores. IDs: $profesoresIds")

        if (profesoresIds.isEmpty()) {
            Log.d("Observaciones", "No hay profesores para cargar, mostrando observaciones")
            filtrarObservaciones()
            return
        }

        var cargados = 0
        profesoresIds.forEach { profesorId ->
            db.collection("users")
                .document(profesorId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val nombreCompleto = document.getString("nombre_completo")
                            ?: document.getString("nombre")
                            ?: "Profesor"

                        Log.d("Observaciones", "Profesor $profesorId: $nombreCompleto")

                        // Actualizar nombre en todas las observaciones de este profesor
                        for (i in observacionesList.indices) {
                            if (observacionesList[i].profesorId == profesorId) {
                                observacionesList[i].profesorNombre = nombreCompleto
                            }
                        }
                    }

                    cargados++
                    if (cargados == profesoresIds.size) {
                        Log.d("Observaciones", "Todos los profesores cargados, mostrando observaciones")
                        filtrarObservaciones()
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("Observaciones", "Error cargando profesor $profesorId", e)
                    cargados++
                    if (cargados == profesoresIds.size) {
                        filtrarObservaciones()
                    }
                }
        }
    }

    private fun filtrarObservaciones() {
        val observacionesFiltradas = if (filtroActual == "todas") {
            observacionesList
        } else {
            observacionesList.filter { it.tipoFalta == filtroActual }
        }

        Log.d("Observaciones", "Mostrando ${observacionesFiltradas.size} observaciones con filtro: $filtroActual")
        mostrarObservaciones(observacionesFiltradas)
    }

    private fun mostrarObservaciones(observaciones: List<ObservacionData>) {
        if (observaciones.isEmpty()) {
            mostrarSinObservaciones()
            return
        }

        tvNoObservaciones.visibility = View.GONE
        scrollObservaciones.visibility = View.VISIBLE

        // Actualizar contador
        val textoFiltro = when (filtroActual) {
            "todas" -> "Total"
            "leve" -> "Leves"
            "media" -> "Medias"
            "grave" -> "Graves"
            else -> "Total"
        }
        tvTotalObservaciones.text = "$textoFiltro: ${observaciones.size}"

        layoutObservaciones.removeAllViews()

        for (observacion in observaciones) {
            val itemView = crearVistaObservacion(observacion)
            layoutObservaciones.addView(itemView)
        }

        Log.d("Observaciones", "Vistas creadas: ${layoutObservaciones.childCount}")
    }

    private fun mostrarSinObservaciones() {
        Log.d("Observaciones", "Mostrando mensaje sin observaciones")
        tvNoObservaciones.visibility = View.VISIBLE
        scrollObservaciones.visibility = View.GONE
        tvTotalObservaciones.text = "Total: 0"
    }

    private fun crearVistaObservacion(observacion: ObservacionData): View {
        val itemView = LayoutInflater.from(this).inflate(
            R.layout.item_observacion_acudiente,
            layoutObservaciones,
            false
        )

        val cardObservacion = itemView.findViewById<CardView>(R.id.cardObservacion)
        val viewColorFalta = itemView.findViewById<View>(R.id.viewColorFalta)
        val tvTipoFalta = itemView.findViewById<TextView>(R.id.tvTipoFalta)
        val tvDescripcion = itemView.findViewById<TextView>(R.id.tvDescripcion)
        val tvProfesor = itemView.findViewById<TextView>(R.id.tvProfesor)
        val tvFecha = itemView.findViewById<TextView>(R.id.tvFecha)
        val iconoFalta = itemView.findViewById<ImageView>(R.id.iconoFalta)

        // Color y estilo según tipo de falta
        when (observacion.tipoFalta) {
            "leve" -> {
                viewColorFalta.setBackgroundColor(ContextCompat.getColor(this, R.color.yellow_warning))
                tvTipoFalta.text = "FALTA LEVE"
                tvTipoFalta.setTextColor(ContextCompat.getColor(this, R.color.yellow_warning))
                iconoFalta.setImageResource(R.drawable.ic_warning)
                iconoFalta.setColorFilter(ContextCompat.getColor(this, R.color.yellow_warning))
            }
            "media" -> {
                viewColorFalta.setBackgroundColor(ContextCompat.getColor(this, R.color.orange_accent))
                tvTipoFalta.text = "FALTA MEDIA"
                tvTipoFalta.setTextColor(ContextCompat.getColor(this, R.color.orange_accent))
                iconoFalta.setImageResource(R.drawable.ic_alert)
                iconoFalta.setColorFilter(ContextCompat.getColor(this, R.color.orange_accent))
            }
            "grave" -> {
                viewColorFalta.setBackgroundColor(ContextCompat.getColor(this, R.color.red_error))
                tvTipoFalta.text = "FALTA GRAVE"
                tvTipoFalta.setTextColor(ContextCompat.getColor(this, R.color.red_error))
                iconoFalta.setImageResource(R.drawable.ic_error)
                iconoFalta.setColorFilter(ContextCompat.getColor(this, R.color.red_error))
            }
        }

        // Descripción
        tvDescripcion.text = observacion.descripcion

        // Profesor
        tvProfesor.text = if (observacion.profesorNombre.isNotEmpty()) {
            "Por: Prof. ${observacion.profesorNombre}"
        } else {
            "Por: Profesor"
        }

        // Fecha
        val sdf = SimpleDateFormat("dd 'de' MMMM 'de' yyyy, hh:mm a", Locale("es", "ES"))
        tvFecha.text = sdf.format(observacion.fecha)

        return itemView
    }
}