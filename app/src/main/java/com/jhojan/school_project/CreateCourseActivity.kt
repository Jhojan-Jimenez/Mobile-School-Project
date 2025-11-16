package com.jhojan.school_project

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.jhojan.school_project.databinding.ActivityCreateCourseBinding

class CreateCourseActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateCourseBinding
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateCourseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()

        setupListeners()
    }

    private fun setupListeners() {
        binding.btnCreateCourse.setOnClickListener {
            createCourse()
        }

        binding.btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun createCourse() {
        val nombre = binding.etNombre.text.toString().trim()
        val codigo = binding.etCodigo.text.toString().trim()

        if (!validateFields(nombre, codigo)) {
            return
        }

        showLoading(true)

        // Verificar si el código ya existe
        db.collection("courses")
            .whereEqualTo("codigo", codigo)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    showLoading(false)
                    binding.tilCodigo.error = "Este código ya existe"
                } else {
                    // Crear el curso
                    val courseData = hashMapOf(
                        "nombre" to nombre,
                        "codigo" to codigo
                    )

                    db.collection("courses")
                        .add(courseData)
                        .addOnSuccessListener {
                            showLoading(false)
                            Toast.makeText(
                                this,
                                "Curso creado exitosamente",
                                Toast.LENGTH_LONG
                            ).show()
                            finish()
                        }
                        .addOnFailureListener { e ->
                            showLoading(false)
                            Toast.makeText(
                                this,
                                "Error al crear curso: ${e.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                }
            }
            .addOnFailureListener { e ->
                showLoading(false)
                Toast.makeText(
                    this,
                    "Error al verificar código: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun validateFields(nombre: String, codigo: String): Boolean {
        var isValid = true

        if (nombre.isEmpty()) {
            binding.tilNombre.error = "El nombre es requerido"
            isValid = false
        } else {
            binding.tilNombre.error = null
        }

        if (codigo.isEmpty()) {
            binding.tilCodigo.error = "El código es requerido"
            isValid = false
        } else {
            binding.tilCodigo.error = null
        }

        return isValid
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnCreateCourse.isEnabled = !show
        binding.btnCancel.isEnabled = !show
    }
}
