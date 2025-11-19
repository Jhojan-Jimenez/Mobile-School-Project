package com.jhojan.school_project

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class DetalleTareaAcudienteActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore

    // Views
    private lateinit var iconBack: ImageView
    private lateinit var tvTitulo: TextView
    private lateinit var tvMateria: TextView
    private lateinit var tvProfesor: TextView
    private lateinit var tvFechaEntrega: TextView
    private lateinit var tvDescripcion: TextView
    private lateinit var tvEstado: TextView
    private lateinit var iconEstado: ImageView
    private lateinit var tvNota: TextView
    private lateinit var layoutNota: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_tarea_acudiente)

        db = FirebaseFirestore.getInstance()

        initViews()
        setupBackButton()
        cargarDatosTarea()
    }

    private fun initViews() {
        iconBack = findViewById(R.id.iconBack)
        tvTitulo = findViewById(R.id.tvTitulo)
        tvMateria = findViewById(R.id.tvMateria)
        tvProfesor = findViewById(R.id.tvProfesor)
        tvFechaEntrega = findViewById(R.id.tvFechaEntrega)
        tvDescripcion = findViewById(R.id.tvDescripcion)
        tvEstado = findViewById(R.id.tvEstado)
        iconEstado = findViewById(R.id.iconEstado)
        tvNota = findViewById(R.id.tvNota)
        layoutNota = findViewById(R.id.layoutNota)
    }

    private fun setupBackButton() {
        iconBack.setOnClickListener {
            finish()
        }
    }

    private fun cargarDatosTarea() {
        val titulo = intent.getStringExtra("titulo") ?: ""
        val descripcion = intent.getStringExtra("descripcion") ?: ""
        val materia = intent.getStringExtra("materia") ?: "Sin materia"
        val profesor = intent.getStringExtra("profesor")
        val fechaEntregaMillis = intent.getLongExtra("fecha_entrega", 0L)
        val nota = intent.getDoubleExtra("nota", 0.0)
        val completada = intent.getBooleanExtra("completada", false)

        // Título
        tvTitulo.text = titulo

        // Materia
        tvMateria.text = materia

        // Profesor
        if (!profesor.isNullOrEmpty()) {
            cargarNombreProfesor(profesor)
        } else {
            tvProfesor.text = "Profesor no asignado"
        }

        // Fecha de entrega
        if (fechaEntregaMillis > 0) {
            val fecha = Date(fechaEntregaMillis)
            val sdfFecha = SimpleDateFormat("EEEE, d 'de' MMMM 'de' yyyy", Locale("es", "ES"))
            val sdfHora = SimpleDateFormat("h:mm a", Locale.getDefault())

            tvFechaEntrega.text = "${sdfFecha.format(fecha)} a las ${sdfHora.format(fecha)}"
        }

        // Descripción
        if (descripcion.isNotEmpty()) {
            tvDescripcion.text = descripcion
        } else {
            tvDescripcion.text = "Sin descripción"
            tvDescripcion.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))
        }

        // Estado
        if (completada) {
            iconEstado.setImageResource(R.drawable.ic_check_circle)
            iconEstado.setColorFilter(ContextCompat.getColor(this, R.color.green_success))
            tvEstado.text = "Completada"
            tvEstado.setTextColor(ContextCompat.getColor(this, R.color.green_success))

            // Mostrar nota si existe
            if (nota > 0) {
                layoutNota.visibility = View.VISIBLE
                tvNota.text = nota.toString()

                val colorNota = when {
                    nota >= 4.0 -> R.color.green_success
                    nota >= 3.0 -> R.color.orange_accent
                    else -> R.color.red_error
                }
                tvNota.setTextColor(ContextCompat.getColor(this, colorNota))
            } else {
                layoutNota.visibility = View.GONE
            }
        } else {
            iconEstado.setImageResource(R.drawable.ic_clock)
            iconEstado.setColorFilter(ContextCompat.getColor(this, R.color.orange_accent))
            tvEstado.text = "Pendiente"
            tvEstado.setTextColor(ContextCompat.getColor(this, R.color.orange_accent))
            layoutNota.visibility = View.GONE
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
}