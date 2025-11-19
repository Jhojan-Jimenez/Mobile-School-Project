package com.jhojan.school_project

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.jhojan.school_project.databinding.ActivityTaskDetailBinding
import java.text.SimpleDateFormat
import java.util.Locale

class TaskDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTaskDetailBinding
    private lateinit var db: FirebaseFirestore
    private var taskId: String = ""
    private var attachedFileName: String? = null

    // Launcher para seleccionar archivos
    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            handleFileSelected(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()

        setupToolbar()
        loadTaskData()
        setupFileAttachment()
    }

    private fun setupToolbar() {
        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun loadTaskData() {
        // Obtener datos del intent
        taskId = intent.getStringExtra("TASK_ID") ?: ""
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

        // Mostrar botón de completar y sección de adjuntar solo si la tarea no está completada
        if (!completada && taskId.isNotEmpty()) {
            binding.btnMarkComplete.visibility = View.VISIBLE
            binding.btnMarkComplete.setOnClickListener {
                markTaskAsCompleted()
            }

            // Mostrar sección de adjuntar archivo
            binding.tvAttachLabel.visibility = View.VISIBLE
            binding.btnAttachFile.visibility = View.VISIBLE

            // Ocultar botón de desmarcar
            binding.btnUnmarkComplete.visibility = View.GONE
        } else if (completada && taskId.isNotEmpty()) {
            // Si está completada, mostrar botón para desmarcar
            binding.btnMarkComplete.visibility = View.GONE
            binding.tvAttachLabel.visibility = View.GONE
            binding.btnAttachFile.visibility = View.GONE

            binding.btnUnmarkComplete.visibility = View.VISIBLE
            binding.btnUnmarkComplete.setOnClickListener {
                unmarkTaskAsCompleted()
            }
        } else {
            // No hay taskId válido, ocultar todos los botones
            binding.btnMarkComplete.visibility = View.GONE
            binding.btnUnmarkComplete.visibility = View.GONE
            binding.tvAttachLabel.visibility = View.GONE
            binding.btnAttachFile.visibility = View.GONE
        }
    }

    private fun setupFileAttachment() {
        // Botón para seleccionar archivo
        binding.btnAttachFile.setOnClickListener {
            openFilePicker()
        }

        // Botón para eliminar archivo adjunto
        binding.btnRemoveFile.setOnClickListener {
            removeAttachedFile()
        }
    }

    private fun openFilePicker() {
        try {
            filePickerLauncher.launch("*/*")
        } catch (e: Exception) {
            Toast.makeText(this, "Error al abrir selector de archivos", Toast.LENGTH_SHORT).show()
            Log.e("TaskDetailActivity", "Error opening file picker", e)
        }
    }

    private fun handleFileSelected(uri: Uri) {
        // Obtener el nombre del archivo
        val fileName = getFileName(uri)
        attachedFileName = fileName

        // Mostrar el archivo seleccionado
        binding.tvAttachedFileName.text = fileName
        binding.cardAttachedFile.visibility = View.VISIBLE

        Toast.makeText(this, "Archivo seleccionado: $fileName", Toast.LENGTH_SHORT).show()
        Log.d("TaskDetailActivity", "Archivo adjuntado: $fileName (simulación)")
    }

    private fun getFileName(uri: Uri): String {
        var fileName = "archivo_adjunto"

        try {
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1 && cursor.moveToFirst()) {
                    fileName = cursor.getString(nameIndex)
                }
            }
        } catch (e: Exception) {
            Log.e("TaskDetailActivity", "Error getting file name", e)
            // Si falla, usar el último segmento del path
            fileName = uri.lastPathSegment ?: "archivo_adjunto"
        }

        return fileName
    }

    private fun removeAttachedFile() {
        attachedFileName = null
        binding.cardAttachedFile.visibility = View.GONE
        Toast.makeText(this, "Archivo eliminado", Toast.LENGTH_SHORT).show()
    }

    private fun markTaskAsCompleted() {
        if (taskId.isEmpty()) {
            Toast.makeText(this, "Error: ID de tarea no válido", Toast.LENGTH_SHORT).show()
            return
        }

        // Nota: El archivo adjunto es solo una simulación y no se guarda
        if (attachedFileName != null) {
            Log.d("TaskDetailActivity", "Archivo adjuntado (simulación): $attachedFileName - No se guardará")
        }

        // Actualizar el campo completada en Firebase
        db.collection("tareas")
            .document(taskId)
            .update("completada", true)
            .addOnSuccessListener {
                Toast.makeText(
                    this,
                    "Tarea marcada como completada exitosamente",
                    Toast.LENGTH_SHORT
                ).show()

                // Actualizar la UI
                binding.tvTaskStatus.text = "Estado: Completada"
                binding.btnMarkComplete.visibility = View.GONE
                binding.tvAttachLabel.visibility = View.GONE
                binding.btnAttachFile.visibility = View.GONE
                binding.cardAttachedFile.visibility = View.GONE
                binding.btnUnmarkComplete.visibility = View.VISIBLE

                Log.d("TaskDetailActivity", "Tarea $taskId marcada como completada")
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Error al marcar tarea como completada: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                Log.e("TaskDetailActivity", "Error al actualizar tarea", e)
            }
    }

    private fun unmarkTaskAsCompleted() {
        if (taskId.isEmpty()) {
            Toast.makeText(this, "Error: ID de tarea no válido", Toast.LENGTH_SHORT).show()
            return
        }

        // Actualizar el campo completada en Firebase a false
        db.collection("tareas")
            .document(taskId)
            .update("completada", false)
            .addOnSuccessListener {
                Toast.makeText(
                    this,
                    "Tarea desmarcada como completada exitosamente",
                    Toast.LENGTH_SHORT
                ).show()

                // Actualizar la UI
                binding.tvTaskStatus.text = "Estado: Próximamente"
                binding.btnUnmarkComplete.visibility = View.GONE
                binding.btnMarkComplete.visibility = View.VISIBLE
                binding.tvAttachLabel.visibility = View.VISIBLE
                binding.btnAttachFile.visibility = View.VISIBLE

                Log.d("TaskDetailActivity", "Tarea $taskId desmarcada como completada")
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Error al desmarcar tarea: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                Log.e("TaskDetailActivity", "Error al actualizar tarea", e)
            }
    }
}
