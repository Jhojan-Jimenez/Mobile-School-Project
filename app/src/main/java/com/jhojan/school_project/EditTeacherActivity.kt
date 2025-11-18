package com.jhojan.school_project

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jhojan.school_project.databinding.ActivityEditTeacherBinding

class EditTeacherActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditTeacherBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var userId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditTeacherBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        userId = intent.getStringExtra("USER_ID") ?: ""

        if (userId.isEmpty()) {
            Toast.makeText(this, "Error: ID de usuario no válido", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadTeacherData()
        setupListeners()
    }

    private fun loadTeacherData() {
        showLoading(true)

        db.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val nombreCompleto = document.getString("nombre_completo") ?: ""
                    val correo = document.getString("correo") ?: ""
                    val telefono = document.getString("telefono") ?: ""
                    val especialidad = document.getString("especialidad") ?: ""
                    val activo = document.getBoolean("activo") ?: true

                    binding.etNombreCompleto.setText(nombreCompleto)
                    binding.etCorreo.setText(correo)
                    binding.etTelefono.setText(telefono)
                    binding.etEspecialidad.setText(especialidad)
                    binding.switchActivo.isChecked = activo
                }
                showLoading(false)
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
        binding.btnSaveTeacher.setOnClickListener {
            saveTeacher()
        }

        binding.btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun saveTeacher() {
        val nombreCompleto = binding.etNombreCompleto.text.toString().trim()
        val telefono = binding.etTelefono.text.toString().trim()
        val especialidad = binding.etEspecialidad.text.toString().trim()
        val activo = binding.switchActivo.isChecked

        if (!validateFields(nombreCompleto)) {
            return
        }

        showLoading(true)

        val teacherData = hashMapOf(
            "nombre_completo" to nombreCompleto,
            "telefono" to telefono,
            "especialidad" to especialidad,
            "activo" to activo
        )

        db.collection("users")
            .document(userId)
            .update(teacherData as Map<String, Any>)
            .addOnSuccessListener {
                showLoading(false)
                Toast.makeText(
                    this,
                    "Profesor actualizado exitosamente",
                    Toast.LENGTH_LONG
                ).show()
                finish()
            }
            .addOnFailureListener { e ->
                showLoading(false)
                Toast.makeText(
                    this,
                    "Error al actualizar profesor: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun validateFields(nombreCompleto: String): Boolean {
        var isValid = true

        if (nombreCompleto.isEmpty()) {
            binding.tilNombreCompleto.error = "El nombre completo es requerido"
            isValid = false
        } else {
            binding.tilNombreCompleto.error = null
        }

        return isValid
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnSaveTeacher.isEnabled = !show
        binding.btnCancel.isEnabled = !show
    }
}
