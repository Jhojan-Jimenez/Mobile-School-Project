package com.jhojan.school_project

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.*
import android.widget.ImageButton

class ActivityCrearTarea : AppCompatActivity() {

    private lateinit var etTaskTitle: TextInputEditText
    private lateinit var etDescription: TextInputEditText
    private lateinit var etDueDate: TextInputEditText
    private lateinit var btnAttachFile: MaterialButton
    private lateinit var btnCancel: MaterialButton
    private lateinit var btnSaveTask: MaterialButton

    private val calendar = Calendar.getInstance()
    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale("es", "CO"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crear_tarea)

        initializeViews()
        setupListeners()
    }





    private fun initializeViews() {
        etTaskTitle = findViewById(R.id.etTaskTitle)
        etDescription = findViewById(R.id.etDescription)
        etDueDate = findViewById(R.id.etDueDate)
        btnAttachFile = findViewById(R.id.btnAttachFile)
        btnCancel = findViewById(R.id.btnCancel)
        btnSaveTask = findViewById(R.id.btnSaveTask)

        // Configurar toolbar
        findViewById<ImageButton>(R.id.btnBack)?.setOnClickListener {
            onBackPressed()
        }
    }

    private fun setupListeners() {
        // Date Picker
        etDueDate.setOnClickListener {
            showDatePicker()
        }

        // Adjuntar archivo
        btnAttachFile.setOnClickListener {
            openFilePicker()
        }

        // Cancelar
        btnCancel.setOnClickListener {
            finish()
        }

        // Guardar tarea
        btnSaveTask.setOnClickListener {
            saveTask()
        }
    }

    private fun showDatePicker() {
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                updateDateField()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun updateDateField() {
        etDueDate.setText(dateFormatter.format(calendar.time))
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        try {
            startActivityForResult(
                Intent.createChooser(intent, "Seleccionar archivo"),
                FILE_PICKER_REQUEST_CODE
            )
        } catch (ex: Exception) {
            Toast.makeText(
                this,
                "Por favor instala un administrador de archivos",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun saveTask() {
        val title = etTaskTitle.text.toString().trim()
        val description = etDescription.text.toString().trim()
        val dueDate = etDueDate.text.toString().trim()

        // Validaciones
        if (title.isEmpty()) {
            etTaskTitle.error = "El título es requerido"
            etTaskTitle.requestFocus()
            return
        }

        // Aquí guardarías la tarea en tu base de datos o sistema de almacenamiento
        // Por ejemplo: viewModel.saveTask(title, description, dueDate)

        Toast.makeText(this, "Tarea guardada exitosamente", Toast.LENGTH_SHORT).show()
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == FILE_PICKER_REQUEST_CODE && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                // Aquí manejarías el archivo seleccionado
                val fileName = getFileName(uri)
                Toast.makeText(this, "Archivo seleccionado: $fileName", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getFileName(uri: android.net.Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val displayNameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (displayNameIndex != -1) {
                        result = it.getString(displayNameIndex)
                    }
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/') ?: -1
            if (cut != -1) {
                result = result?.substring(cut + 1)
            }
        }
        return result ?: "archivo"
    }

    companion object {
        private const val FILE_PICKER_REQUEST_CODE = 100
    }
}