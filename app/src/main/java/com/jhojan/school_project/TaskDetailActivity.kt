package com.jhojan.school_project

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jhojan.school_project.databinding.ActivityTaskDetailBinding
import java.text.SimpleDateFormat
import java.util.Locale

class TaskDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTaskDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        loadTaskData()
    }

    private fun setupToolbar() {
        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun loadTaskData() {
        // Obtener datos del intent
        val title = intent.getStringExtra("TASK_TITLE") ?: "Tarea"
        val subject = intent.getStringExtra("TASK_SUBJECT") ?: ""
        val dueDate = intent.getLongExtra("TASK_DUE_DATE", System.currentTimeMillis())
        val statusName = intent.getStringExtra("TASK_STATUS") ?: "UPCOMING"
        val description = intent.getStringExtra("TASK_DESCRIPTION") ?: "Sin descripción"

        // Formatear fecha
        val dateFormat = SimpleDateFormat("EEEE, d 'de' MMMM 'de' yyyy", Locale("es", "ES"))
        val formattedDate = dateFormat.format(dueDate)

        // Traducir estado
        val statusText = when (statusName) {
            "UPCOMING" -> "Próximamente"
            "OVERDUE" -> "Vencida"
            "COMPLETED" -> "Completada"
            else -> "Desconocido"
        }

        // Actualizar UI
        binding.tvTaskTitle.text = title
        binding.tvTaskSubject.text = subject
        binding.tvTaskDueDate.text = "Entrega: ${formattedDate.replaceFirstChar { it.uppercase() }}"
        binding.tvTaskStatus.text = "Estado: $statusText"
        binding.tvTaskDescription.text = description
    }
}
