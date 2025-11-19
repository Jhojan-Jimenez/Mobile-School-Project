package com.jhojan.school_project

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jhojan.school_project.databinding.ActivityEventDetailBinding
import java.text.SimpleDateFormat
import java.util.Locale

class EventDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEventDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEventDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        loadEventData()
    }

    private fun setupToolbar() {
        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun loadEventData() {
        // Obtener datos del intent
        val titulo = intent.getStringExtra("EVENT_TITULO") ?: "Evento"
        val fecha = intent.getLongExtra("EVENT_FECHA", System.currentTimeMillis())
        val alcance = intent.getStringExtra("EVENT_ALCANCE") ?: ""
        val cursoNombre = intent.getStringExtra("EVENT_CURSO_NOMBRE") ?: ""
        val asignaturaNombre = intent.getStringExtra("EVENT_ASIGNATURA_NOMBRE") ?: ""
        val estudianteNombre = intent.getStringExtra("EVENT_ESTUDIANTE_NOMBRE") ?: ""

        // Formatear fecha y hora
        val dateFormat = SimpleDateFormat("EEEE, d 'de' MMMM 'de' yyyy", Locale("es", "ES"))
        val formattedDate = dateFormat.format(fecha)

        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val formattedTime = timeFormat.format(fecha)

        // Construir descripción según el alcance
        val description = when (alcance) {
            "Colegio" -> "Evento para todo el colegio"
            "Curso" -> "Evento para el curso: $cursoNombre"
            "Asignatura" -> "Evento para la asignatura: $asignaturaNombre"
            "Estudiante" -> "Evento personal para: $estudianteNombre"
            else -> "Sin descripción"
        }

        // Actualizar UI
        binding.tvEventTitle.text = titulo
        binding.tvEventDate.text = formattedDate.replaceFirstChar { it.uppercase() }
        binding.tvEventTime.text = formattedTime
        binding.tvEventDescription.text = description
    }
}
