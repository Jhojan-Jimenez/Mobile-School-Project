package com.jhojan.school_project

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton

class RevisarTareaActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var rvEstudiantes: RecyclerView
    private lateinit var btnGuardar: MaterialButton
    private lateinit var adapter: EstudianteCalificacionAdapter

    private val estudiantes = listOf(
        Estudiante(
            nombre = "Ana Pérez",
            fechaEntrega = "14 de mayo",
            calificacion = 4.5,
            estado = EstadoEntrega.ENTREGADO
        ),
        Estudiante(
            nombre = "Luis García",
            fechaEntrega = "15 de mayo",
            calificacion = 0.0,
            estado = EstadoEntrega.PENDIENTE
        ),
        Estudiante(
            nombre = "María Rodríguez",
            fechaEntrega = "15 de mayo",
            calificacion = 0.0,
            estado = EstadoEntrega.NO_ENTREGADO
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_revisar_tarea)

        initViews()
        setupToolbar()
        setupRecyclerView()
        setupListeners()
    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        rvEstudiantes = findViewById(R.id.rvEstudiantes)
        btnGuardar = findViewById(R.id.btnGuardar)
    }

    private fun setupToolbar() {
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        adapter = EstudianteCalificacionAdapter(estudiantes.toMutableList())
        rvEstudiantes.layoutManager = LinearLayoutManager(this)
        rvEstudiantes.adapter = adapter
    }

    private fun setupListeners() {
        btnGuardar.setOnClickListener {
            guardarCalificaciones()
        }
    }

    private fun guardarCalificaciones() {
        val calificaciones = adapter.obtenerCalificaciones()

        // Validar calificaciones
        var todasValidas = true
        for ((nombre, calificacion) in calificaciones) {
            if (calificacion < 0.0 || calificacion > 5.0) {
                Toast.makeText(
                    this,
                    "La calificación de $nombre debe estar entre 0.0 y 5.0",
                    Toast.LENGTH_SHORT
                ).show()
                todasValidas = false
                break
            }
        }

        if (todasValidas) {
            // Aquí iría la lógica para guardar en la base de datos o servidor
            Toast.makeText(
                this,
                "Calificaciones guardadas exitosamente",
                Toast.LENGTH_SHORT
            ).show()

            // Opcional: volver a la pantalla anterior
            // finish()
        }
    }
}

// Data class para representar un estudiante
data class Estudiante(
    val nombre: String,
    val fechaEntrega: String,
    var calificacion: Double,
    val estado: EstadoEntrega
)

// Enum para el estado de entrega
enum class EstadoEntrega(val texto: String, val color: Int) {
    ENTREGADO("Entregado", 0xFF4CAF50.toInt()),
    PENDIENTE("Pendiente", 0xFFFF9800.toInt()),
    NO_ENTREGADO("No entregado", 0xFFF44336.toInt())
}