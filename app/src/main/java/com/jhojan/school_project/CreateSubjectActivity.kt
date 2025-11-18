package com.jhojan.school_project

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.jhojan.school_project.databinding.ActivityCreateSubjectBinding

class CreateSubjectActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateSubjectBinding
    private lateinit var db: FirebaseFirestore
    private val coursesList = mutableListOf<Course>()
    private val coursesNames = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateSubjectBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()

        loadCourses()
        setupListeners()
    }

    private fun loadCourses() {
        showLoading(true)

        db.collection("courses")
            .get()
            .addOnSuccessListener { documents ->
                coursesList.clear()
                coursesNames.clear()
                coursesNames.add("Seleccionar curso")

                for (document in documents) {
                    val course = Course(
                        id = document.id,
                        nombre = document.getString("nombre") ?: "",
                        codigo = document.getString("codigo") ?: ""
                    )
                    coursesList.add(course)
                    coursesNames.add(course.nombre)
                }

                setupCourseSpinner()
                showLoading(false)
            }
            .addOnFailureListener { e ->
                showLoading(false)
                Toast.makeText(
                    this,
                    "Error al cargar cursos: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun setupCourseSpinner() {
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            coursesNames
        )
        binding.spinnerCurso.adapter = adapter
    }

    private fun setupListeners() {
        binding.btnCreateSubject.setOnClickListener {
            createSubject()
        }

        binding.btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun createSubject() {
        val nombre = binding.etNombre.text.toString().trim()
        val codigo = binding.etCodigo.text.toString().trim()
        val cursoPosition = binding.spinnerCurso.selectedItemPosition

        if (!validateFields(nombre, codigo, cursoPosition)) {
            return
        }

        showLoading(true)

        // Verificar si el código ya existe
        db.collection("subjects")
            .whereEqualTo("codigo", codigo)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    showLoading(false)
                    binding.tilCodigo.error = "Este código ya existe"
                } else {
                    // Obtener el curso seleccionado
                    val selectedCourse = coursesList[cursoPosition - 1]

                    // Crear la asignatura
                    val subjectData = hashMapOf(
                        "nombre" to nombre,
                        "codigo" to codigo,
                        "curso_id" to selectedCourse.id,
                        "curso_nombre" to selectedCourse.nombre
                    )

                    db.collection("subjects")
                        .add(subjectData)
                        .addOnSuccessListener {
                            showLoading(false)
                            Toast.makeText(
                                this,
                                "Asignatura creada exitosamente",
                                Toast.LENGTH_LONG
                            ).show()
                            finish()
                        }
                        .addOnFailureListener { e ->
                            showLoading(false)
                            Toast.makeText(
                                this,
                                "Error al crear asignatura: ${e.message}",
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

    private fun validateFields(nombre: String, codigo: String, cursoPosition: Int): Boolean {
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

        if (cursoPosition == 0) {
            Toast.makeText(this, "Debe seleccionar un curso", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        return isValid
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnCreateSubject.isEnabled = !show
        binding.btnCancel.isEnabled = !show
    }
}
