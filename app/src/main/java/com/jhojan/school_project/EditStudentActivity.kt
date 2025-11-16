package com.jhojan.school_project

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.jhojan.school_project.databinding.ActivityEditStudentBinding

class EditStudentActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditStudentBinding
    private lateinit var db: FirebaseFirestore
    private var studentId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditStudentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()

        // Obtener el ID del estudiante desde el Intent
        studentId = intent.getStringExtra("STUDENT_ID") ?: ""

        if (studentId.isEmpty()) {
            Toast.makeText(this, "Error: No se encontró el estudiante", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadStudentData()
        setupListeners()
    }

    private fun loadStudentData() {
        showLoading(true)

        db.collection("users")
            .document(studentId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Cargar datos básicos
                    binding.etNombreCompleto.setText(document.getString("nombre_completo") ?: "")
                    binding.etEmail.setText(document.getString("correo") ?: "")
                    binding.etTelefono.setText(document.getString("telefono") ?: "")

                    // Cargar datos específicos de estudiante
                    binding.etGrado.setText(document.getString("grado") ?: "")
                    binding.etGrupo.setText(document.getString("grupo") ?: "")

                    showLoading(false)
                } else {
                    showLoading(false)
                    Toast.makeText(this, "Estudiante no encontrado", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .addOnFailureListener { e ->
                showLoading(false)
                Toast.makeText(
                    this,
                    "Error al cargar datos: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                finish()
            }
    }

    private fun setupListeners() {
        binding.btnUpdateStudent.setOnClickListener {
            updateStudent()
        }

        binding.btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun updateStudent() {
        val nombreCompleto = binding.etNombreCompleto.text.toString().trim()
        val telefono = binding.etTelefono.text.toString().trim()
        val grado = binding.etGrado.text.toString().trim()
        val grupo = binding.etGrupo.text.toString().trim()

        if (!validateFields(nombreCompleto, grado, grupo)) {
            return
        }

        showLoading(true)

        // Preparar datos para actualizar
        val updates = hashMapOf<String, Any>(
            "nombre_completo" to nombreCompleto,
            "telefono" to telefono,
            "grado" to grado,
            "grupo" to grupo
        )

        // Actualizar en Firestore
        db.collection("users")
            .document(studentId)
            .update(updates)
            .addOnSuccessListener {
                showLoading(false)
                Toast.makeText(
                    this,
                    "Estudiante actualizado exitosamente",
                    Toast.LENGTH_LONG
                ).show()
                finish()
            }
            .addOnFailureListener { e ->
                showLoading(false)
                Toast.makeText(
                    this,
                    "Error al actualizar estudiante: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun validateFields(nombreCompleto: String, grado: String, grupo: String): Boolean {
        var isValid = true

        if (nombreCompleto.isEmpty()) {
            binding.tilNombreCompleto.error = "El nombre completo es requerido"
            isValid = false
        } else {
            binding.tilNombreCompleto.error = null
        }

        if (grado.isEmpty()) {
            binding.tilGrado.error = "El grado es requerido"
            isValid = false
        } else {
            binding.tilGrado.error = null
        }

        if (grupo.isEmpty()) {
            binding.tilGrupo.error = "El grupo es requerido"
            isValid = false
        } else {
            binding.tilGrupo.error = null
        }

        return isValid
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnUpdateStudent.isEnabled = !show
        binding.btnCancel.isEnabled = !show
    }
}
