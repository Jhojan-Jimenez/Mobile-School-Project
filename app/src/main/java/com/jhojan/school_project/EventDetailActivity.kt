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
        val title = intent.getStringExtra("EVENT_TITLE") ?: "Evento"
        val date = intent.getLongExtra("EVENT_DATE", System.currentTimeMillis())
        val time = intent.getStringExtra("EVENT_TIME") ?: ""
        val description = intent.getStringExtra("EVENT_DESCRIPTION") ?: "Sin descripción"

        // Formatear fecha
        val dateFormat = SimpleDateFormat("EEEE, d 'de' MMMM 'de' yyyy", Locale("es", "ES"))
        val formattedDate = dateFormat.format(date)

        // Actualizar UI
        binding.tvEventTitle.text = title
        binding.tvEventDate.text = formattedDate.replaceFirstChar { it.uppercase() }
        binding.tvEventTime.text = time
        binding.tvEventDescription.text = description
    }
}
