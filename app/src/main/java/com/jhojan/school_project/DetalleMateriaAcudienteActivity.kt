package com.jhojan.school_project

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class DetalleMateriaAcudienteActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore

    // Views
    private lateinit var iconBack: ImageView
    private lateinit var viewColorHeader: View
    private lateinit var tvNombreMateria: TextView
    private lateinit var tvProfesor: TextView
    private lateinit var tvPromedio: TextView
    private lateinit var progressPromedio: ProgressBar
    private lateinit var tvTotalTareas: TextView
    private lateinit var tvCompletadas: TextView
    private lateinit var tvPendientes: TextView
    private lateinit var layoutTareas: LinearLayout
    private lateinit var scrollTareas: ScrollView
    private lateinit var tvNoTareas: TextView

    // Datos
    private var materiaId: String = ""
    private var estudianteId: String = ""
    private val tareasList = mutableListOf<TareaDetalle>()

    data class TareaDetalle(
        val id: String,
        val titulo: String,
        val descripcion: String,
        val fechaEntrega: Date,
        val completada: Boolean,
        val nota: Double
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_materia_acudiente)

        db = FirebaseFirestore.getInstance()

        materiaId = intent.getStringExtra("materia_id") ?: ""
        estudianteId = intent.getStringExtra("estudiante_id") ?: ""

        initViews()
        setupBackButton()
        cargarDatosMateria()
        cargarTareasMateria()
    }

    private fun initViews() {
        iconBack = findViewById(R.id.iconBack)
        viewColorHeader = findViewById(R.id.viewColorHeader)
        tvNombreMateria = findViewById(R.id.tvNombreMateria)
        tvProfesor = findViewById(R.id.tvProfesor)
        tvPromedio = findViewById(R.id.tvPromedio)
        progressPromedio = findViewById(R.id.progressPromedio)
        tvTotalTareas = findViewById(R.id.tvTotalTareas)
        tvCompletadas = findViewById(R.id.tvCompletadas)
        tvPendientes = findViewById(R.id.tvPendientes)
        layoutTareas = findViewById(R.id.layoutTareas)
        scrollTareas = findViewById(R.id.scrollTareas)
        tvNoTareas = findViewById(R.id.tvNoTareas)
    }

    private fun setupBackButton() {
        iconBack.setOnClickListener {
            finish()
        }
    }

    private fun cargarDatosMateria() {
        val nombreMateria = intent.getStringExtra("materia_nombre") ?: "Materia"
        val color = intent.getStringExtra("materia_color")
        val profesorId = intent.getStringExtra("materia_profesor")

        // Nombre
        tvNombreMateria.text = nombreMateria

        // Color
        if (!color.isNullOrEmpty()) {
            try {
                viewColorHeader.setBackgroundColor(android.graphics.Color.parseColor(color))
            } catch (e: Exception) {
                viewColorHeader.setBackgroundColor(ContextCompat.getColor(this, R.color.blue_primary))
            }
        }

        // Profesor
        if (!profesorId.isNullOrEmpty()) {
            cargarNombreProfesor(profesorId)
        } else {
            tvProfesor.text = "Sin profesor asignado"
        }
    }

    private fun cargarNombreProfesor(profesorId: String) {
        db.collection("users")
            .document(profesorId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val nombreCompleto = document.getString("nombre_completo")
                        ?: document.getString("nombre")
                        ?: "Profesor"

                    tvProfesor.text = "Prof. $nombreCompleto"
                } else {
                    tvProfesor.text = "Profesor no encontrado"
                }
            }
            .addOnFailureListener {
                tvProfesor.text = "Error al cargar profesor"
            }
    }

    private fun cargarTareasMateria() {
        tareasList.clear()

        db.collection("tareas")
            .whereEqualTo("estudiante", estudianteId)
            .whereEqualTo("materia", materiaId)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val titulo = document.getString("titulo") ?: "Sin título"
                    val descripcion = document.getString("descripcion") ?: ""
                    val fechaEntrega = document.getTimestamp("fecha_entrega")?.toDate() ?: Date()
                    val completada = document.getBoolean("completada") ?: false
                    val nota = document.getDouble("nota") ?: 0.0

                    tareasList.add(
                        TareaDetalle(
                            id = document.id,
                            titulo = titulo,
                            descripcion = descripcion,
                            fechaEntrega = fechaEntrega,
                            completada = completada,
                            nota = nota
                        )
                    )
                }

                // Ordenar por fecha (más recientes primero)
                tareasList.sortByDescending { it.fechaEntrega }

                actualizarEstadisticas()
                mostrarTareas()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al cargar tareas: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun actualizarEstadisticas() {
        val total = tareasList.size
        val completadas = tareasList.count { it.completada }
        val pendientes = total - completadas

        tvTotalTareas.text = total.toString()
        tvCompletadas.text = completadas.toString()
        tvPendientes.text = pendientes.toString()

        // Calcular promedio
        val tareasConNota = tareasList.filter { it.completada && it.nota > 0 }
        if (tareasConNota.isNotEmpty()) {
            val promedio = tareasConNota.map { it.nota }.average()
            tvPromedio.text = String.format("%.1f", promedio)
            progressPromedio.progress = ((promedio / 5.0) * 100).toInt()

            val colorPromedio = when {
                promedio >= 4.0 -> R.color.green_success
                promedio >= 3.0 -> R.color.orange_accent
                else -> R.color.red_error
            }
            tvPromedio.setTextColor(ContextCompat.getColor(this, colorPromedio))
        } else {
            tvPromedio.text = "S/N"
            tvPromedio.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))
            progressPromedio.progress = 0
        }
    }

    private fun mostrarTareas() {
        layoutTareas.removeAllViews()

        if (tareasList.isEmpty()) {
            tvNoTareas.visibility = View.VISIBLE
            scrollTareas.visibility = View.GONE
            return
        }

        tvNoTareas.visibility = View.GONE
        scrollTareas.visibility = View.VISIBLE

        for (tarea in tareasList) {
            val itemView = crearVistaTarea(tarea)
            layoutTareas.addView(itemView)
        }
    }

    private fun crearVistaTarea(tarea: TareaDetalle): View {
        val itemView = LayoutInflater.from(this).inflate(
            R.layout.item_tarea_detalle_materia,
            layoutTareas,
            false
        )

        val cardTarea = itemView.findViewById<CardView>(R.id.cardTarea)
        val iconEstado = itemView.findViewById<ImageView>(R.id.iconEstado)
        val tvTitulo = itemView.findViewById<TextView>(R.id.tvTituloTarea)
        val tvFecha = itemView.findViewById<TextView>(R.id.tvFechaTarea)
        val tvNota = itemView.findViewById<TextView>(R.id.tvNotaTarea)
        val layoutNota = itemView.findViewById<LinearLayout>(R.id.layoutNotaTarea)

        // Título
        tvTitulo.text = tarea.titulo

        // Fecha
        val sdf = SimpleDateFormat("d MMM yyyy, h:mm a", Locale("es", "ES"))
        tvFecha.text = sdf.format(tarea.fechaEntrega)

        // Estado y nota
        if (tarea.completada) {
            iconEstado.setImageResource(R.drawable.ic_check_circle)
            iconEstado.setColorFilter(ContextCompat.getColor(this, R.color.green_success))

            if (tarea.nota > 0) {
                layoutNota.visibility = View.VISIBLE
                tvNota.text = tarea.nota.toString()

                val colorNota = when {
                    tarea.nota >= 4.0 -> R.color.green_success
                    tarea.nota >= 3.0 -> R.color.orange_accent
                    else -> R.color.red_error
                }
                tvNota.setTextColor(ContextCompat.getColor(this, colorNota))
            } else {
                layoutNota.visibility = View.GONE
            }
        } else {
            iconEstado.setImageResource(R.drawable.ic_clock)
            iconEstado.setColorFilter(ContextCompat.getColor(this, R.color.orange_accent))
            layoutNota.visibility = View.GONE
        }

        // Click para ver detalle
        cardTarea.setOnClickListener {
            val intent = Intent(this, DetalleTareaAcudienteActivity::class.java)
            intent.putExtra("tarea_id", tarea.id)
            intent.putExtra("titulo", tarea.titulo)
            intent.putExtra("descripcion", tarea.descripcion)
            intent.putExtra("materia", materiaId)
            intent.putExtra("fecha_entrega", tarea.fechaEntrega.time)
            intent.putExtra("nota", tarea.nota)
            intent.putExtra("completada", tarea.completada)
            startActivity(intent)
        }

        return itemView
    }
}