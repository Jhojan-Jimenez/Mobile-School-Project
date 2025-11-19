package com.jhojan.school_project

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
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
        val titulo = intent.getStringExtra("TASK_TITULO") ?: "Tarea"
        val descripcion = intent.getStringExtra("TASK_DESCRIPCION") ?: "Sin descripción"
        val fechaEntrega = intent.getLongExtra("TASK_FECHA_ENTREGA", System.currentTimeMillis())
        val link = intent.getStringExtra("TASK_LINK") ?: ""
        val subjectName = intent.getStringExtra("TASK_SUBJECT_NAME") ?: "Materia"
        val nota = intent.getDoubleExtra("TASK_NOTA", 0.0)
        val completada = intent.getBooleanExtra("TASK_COMPLETADA", false)
        val statusName = intent.getStringExtra("TASK_STATUS") ?: "UPCOMING"

        // Formatear fecha
        val dateFormat = SimpleDateFormat("EEEE, d 'de' MMMM 'de' yyyy", Locale("es", "ES"))
        val formattedDate = dateFormat.format(fechaEntrega)

        // Traducir estado
        val statusText = when (statusName) {
            "UPCOMING" -> "Próximamente"
            "OVERDUE" -> "Vencida"
            "COMPLETED" -> "Completada"
            else -> "Desconocido"
        }

        // Actualizar UI
        binding.tvTaskTitle.text = titulo
        binding.tvTaskSubject.text = subjectName
        binding.tvTaskDueDate.text = "Entrega: ${formattedDate.replaceFirstChar { it.uppercase() }}"
        binding.tvTaskStatus.text = "Estado: $statusText"
        binding.tvTaskDescription.text = descripcion

        // Mostrar nota si está completada
        if (completada && nota > 0) {
            binding.tvTaskGrade.visibility = View.VISIBLE
            binding.tvTaskGrade.text = "Nota: ${String.format("%.1f", nota)}"
        } else {
            binding.tvTaskGrade.visibility = View.GONE
        }

        // Mostrar link si existe
        if (link.isNotEmpty()) {
            binding.tvLinkLabel.visibility = View.VISIBLE
            binding.tvTaskLink.visibility = View.VISIBLE
            binding.tvTaskLink.text = link
            binding.tvTaskLink.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
                startActivity(intent)
            }
        } else {
            binding.tvLinkLabel.visibility = View.GONE
            binding.tvTaskLink.visibility = View.GONE
        }
    }
}
