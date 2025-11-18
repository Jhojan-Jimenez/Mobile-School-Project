package com.jhojan.school_project

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.jhojan.school_project.databinding.ActivityEditSubjectBinding

class EditSubjectActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditSubjectBinding
    private lateinit var db: FirebaseFirestore
    private val coursesList = mutableListOf<Course>()
    private val coursesNames = mutableListOf<String>()
    private var subjectId: String = ""
    private var originalCodigo: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditSubjectBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()

        subjectId = intent.getStringExtra("SUBJECT_ID") ?: ""

        if (subjectId.isEmpty()) {
            Toast.makeText(this, "Error: ID de asignatura no válido", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadCourses()
        loadSubjectData()
        setupListeners()
    }

    private fun loadCourses() {
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
            }
            .addOnFailureListener { e ->
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
            R.layout.spinner_item,
            coursesNames
        )
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        binding.spinnerCurso.adapter = adapter
    }

    private fun loadSubjectData() {
        showLoading(true)

        db.collection("subjects")
            .document(subjectId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val nombre = document.getString("nombre") ?: ""
                    val codigo = document.getString("codigo") ?: ""
                    val cursoId = document.getString("curso_id") ?: ""

                    originalCodigo = codigo

                    binding.etNombre.setText(nombre)
                    binding.etCodigo.setText(codigo)

                    // Seleccionar el curso correspondiente en el spinner
                    val courseIndex = coursesList.indexOfFirst { it.id == cursoId }
                    if (courseIndex >= 0) {
                        binding.spinnerCurso.setSelection(courseIndex + 1) // +1 por el item "Seleccionar curso"
                    }
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
        binding.btnSaveSubject.setOnClickListener {
            saveSubject()
        }

        binding.btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun saveSubject() {
        val nombre = binding.etNombre.text.toString().trim()
        val codigo = binding.etCodigo.text.toString().trim()
        val cursoPosition = binding.spinnerCurso.selectedItemPosition

        if (!validateFields(nombre, codigo, cursoPosition)) {
            return
        }

        showLoading(true)

        // Verificar si el código cambió y si ya existe
        if (codigo != originalCodigo) {
            db.collection("subjects")
                .whereEqualTo("codigo", codigo)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        showLoading(false)
                        binding.tilCodigo.error = "Este código ya existe"
                    } else {
                        updateSubject(nombre, codigo, cursoPosition)
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
        } else {
            updateSubject(nombre, codigo, cursoPosition)
        }
    }

    private fun updateSubject(nombre: String, codigo: String, cursoPosition: Int) {
        val selectedCourse = coursesList[cursoPosition - 1]

        val subjectData = hashMapOf(
            "nombre" to nombre,
            "codigo" to codigo,
            "curso_id" to selectedCourse.id,
            "curso_nombre" to selectedCourse.nombre
        )

        db.collection("subjects")
            .document(subjectId)
            .update(subjectData as Map<String, Any>)
            .addOnSuccessListener {
                showLoading(false)
                Toast.makeText(
                    this,
                    "Asignatura actualizada exitosamente",
                    Toast.LENGTH_LONG
                ).show()
                finish()
            }
            .addOnFailureListener { e ->
                showLoading(false)
                Toast.makeText(
                    this,
                    "Error al actualizar asignatura: ${e.message}",
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
        binding.btnSaveSubject.isEnabled = !show
        binding.btnCancel.isEnabled = !show
    }
}
