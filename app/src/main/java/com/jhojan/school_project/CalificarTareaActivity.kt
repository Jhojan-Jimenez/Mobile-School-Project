package com.jhojan.school_project

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.jhojan.school_project.TeacherHeader
import com.jhojan.school_project.TeacherBottomNavigationView
import java.text.SimpleDateFormat
import java.util.*

class CalificarTareaActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore

    // Header y Footer
    private lateinit var teacherHeader: TeacherHeader
    private lateinit var bottomNav: TeacherBottomNavigationView

    // Views
    private lateinit var tvTitulo: TextView
    private lateinit var tvDescripcion: TextView
    private lateinit var tvFechaEntrega: TextView
    private lateinit var tvEstadoTarea: TextView
    private lateinit var btnAbrirLink: Button
    private lateinit var seekBarNota: SeekBar
    private lateinit var tvNotaActual: TextView
    private lateinit var btnGuardarNota: Button
    private lateinit var btnMarcarCompletada: Button

    private lateinit var tvEstudiante: TextView

    // Datos de la tarea
    private var tareaId: String = ""
    private var profesorId: String = ""
    private var notaActual: Int = 0
    private var completada: Boolean = false
    private var linkTarea: String? = null




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calificar_tarea)

        // Inicializar Firestore
        db = FirebaseFirestore.getInstance()

        // Obtener ID del profesor
        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        profesorId = prefs.getString("profesor_id", "") ?: ""


        if (profesorId.isEmpty()) {
            profesorId = "ZW6kOK1PaGVcteR4N9mzSQlcxjd2"
        }

        // Obtener datos del Intent
        tareaId = intent.getStringExtra("tarea_id") ?: ""

        // Inicializar vistas
        initViews()

        // Configurar Header y Footer
        setupHeaderFooter()

        // Cargar datos de la tarea
        cargarDatosTarea()

        // Configurar listeners
        setupListeners()
    }

    private fun initViews() {
        teacherHeader = findViewById(R.id.teacherHeader)
        bottomNav = findViewById(R.id.bottomNav)
        tvTitulo = findViewById(R.id.tvTitulo)
        tvDescripcion = findViewById(R.id.tvDescripcion)
        tvFechaEntrega = findViewById(R.id.tvFechaEntrega)
        tvEstadoTarea = findViewById(R.id.tvEstadoTarea)
        btnAbrirLink = findViewById(R.id.btnAbrirLink)
        seekBarNota = findViewById(R.id.seekBarNota)
        tvNotaActual = findViewById(R.id.tvNotaActual)
        btnGuardarNota = findViewById(R.id.btnGuardarNota)
        btnMarcarCompletada = findViewById(R.id.btnMarcarCompletada)
        tvEstudiante = findViewById(R.id.tvEstudiante)

    }

    private fun setupHeaderFooter() {
        teacherHeader.loadTeacherData(profesorId)
        teacherHeader.setOnBackClickListener {
            finish()
        }

        bottomNav.setActiveItem(TeacherBottomNavigationView.NavigationItem.TAREAS)
    }

    private fun cargarDatosTarea() {
        // Obtener datos del Intent
        val titulo = intent.getStringExtra("titulo") ?: ""
        val estudiante = intent.getStringExtra("estudiante") ?: ""
        val descripcion = intent.getStringExtra("descripcion") ?: ""
        val fechaEntregaMillis = intent.getLongExtra("fecha_entrega", 0)
        notaActual = intent.getLongExtra("nota", 0).toInt()
        completada = intent.getBooleanExtra("completada", false)
        linkTarea = intent.getStringExtra("link")

        // Mostrar datos
        tvTitulo.text = titulo
        tvEstudiante.text = estudiante
        tvDescripcion.text = descripcion

        // Formatear fecha
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val fecha = Date(fechaEntregaMillis)
        tvFechaEntrega.text = "Fecha de entrega: ${sdf.format(fecha)}"

        // Estado
        actualizarEstado()

        // Configurar SeekBar con la nota actual
        seekBarNota.progress = notaActual
        tvNotaActual.text = "$notaActual/100"

        // Mostrar/ocultar botón de link
        if (linkTarea != null && linkTarea!!.isNotEmpty()) {
            btnAbrirLink.visibility = View.VISIBLE
        } else {
            btnAbrirLink.visibility = View.GONE
        }
    }

    private fun actualizarEstado() {
        if (completada) {
            tvEstadoTarea.text = "✓ Tarea Completada"
            tvEstadoTarea.setTextColor(android.graphics.Color.parseColor("#4CAF50"))
            btnMarcarCompletada.text = "Marcar como Pendiente"
        } else {
            tvEstadoTarea.text = "○ Tarea Pendiente"
            tvEstadoTarea.setTextColor(android.graphics.Color.parseColor("#FF9800"))
            btnMarcarCompletada.text = "Marcar como Completada"
        }
    }

    private fun setupListeners() {
        // SeekBar para la nota
        seekBarNota.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                notaActual = progress
                tvNotaActual.text = "$progress/100"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Botón Guardar Nota
        btnGuardarNota.setOnClickListener {
            guardarNota()
        }

        // Botón Marcar Completada/Pendiente
        btnMarcarCompletada.setOnClickListener {
            cambiarEstadoCompletada()
        }

        // Botón Abrir Link
        btnAbrirLink.setOnClickListener {
            abrirLink()
        }
    }

    private fun guardarNota() {
        if (tareaId.isEmpty()) {
            Toast.makeText(this, "Error: ID de tarea no válido", Toast.LENGTH_SHORT).show()
            return
        }

        btnGuardarNota.isEnabled = false
        btnGuardarNota.text = "Guardando..."

        db.collection("tareas")
            .document(tareaId)
            .update("nota", notaActual.toLong())
            .addOnSuccessListener {
                Toast.makeText(this, "Nota guardada: $notaActual/100", Toast.LENGTH_SHORT).show()
                btnGuardarNota.isEnabled = true
                btnGuardarNota.text = "Guardar Nota"
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al guardar nota: ${e.message}", Toast.LENGTH_SHORT).show()
                btnGuardarNota.isEnabled = true
                btnGuardarNota.text = "Guardar Nota"
            }
    }

    private fun cambiarEstadoCompletada() {
        if (tareaId.isEmpty()) {
            Toast.makeText(this, "Error: ID de tarea no válido", Toast.LENGTH_SHORT).show()
            return
        }

        btnMarcarCompletada.isEnabled = false

        val nuevoEstado = !completada

        db.collection("tareas")
            .document(tareaId)
            .update("completada", nuevoEstado)
            .addOnSuccessListener {
                completada = nuevoEstado
                actualizarEstado()

                val mensaje = if (completada) "Tarea marcada como completada" else "Tarea marcada como pendiente"
                Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()

                btnMarcarCompletada.isEnabled = true
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al cambiar estado: ${e.message}", Toast.LENGTH_SHORT).show()
                btnMarcarCompletada.isEnabled = true
            }
    }

    private fun abrirLink() {
        if (linkTarea != null && linkTarea!!.isNotEmpty()) {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(linkTarea))
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "Error al abrir el enlace", Toast.LENGTH_SHORT).show()
            }
        }
    }
}