package com.jhojan.school_project

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.jhojan.school_project.databinding.ActivityEditEventBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class EditEventActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditEventBinding
    private lateinit var db: FirebaseFirestore

    private val coursesList = mutableListOf<Course>()
    private val coursesNames = mutableListOf<String>()

    private val subjectsList = mutableListOf<Subject>()
    private val subjectsNames = mutableListOf<String>()

    private val studentsList = mutableListOf<Student>()
    private val studentsNames = mutableListOf<String>()

    private val alcanceOptions = listOf("Seleccionar alcance", "Colegio", "Curso", "Asignatura", "Estudiante")

    private var conditionalSpinner: Spinner? = null
    private var conditionalLabel: TextView? = null

    private var selectedDateInMillis: Long? = null
    private var eventId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditEventBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()

        eventId = intent.getStringExtra("EVENT_ID") ?: ""
        if (eventId.isEmpty()) {
            Toast.makeText(this, "Error: ID de evento no válido", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadData()
        setupAlcanceSpinner()
        setupDatePicker()
        setupListeners()
        loadEventData()
    }

    private fun loadData() {
        loadCourses()
        loadSubjects()
        loadStudents()
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
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Error al cargar cursos: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun loadSubjects() {
        db.collection("subjects")
            .get()
            .addOnSuccessListener { documents ->
                subjectsList.clear()
                subjectsNames.clear()
                subjectsNames.add("Seleccionar asignatura")

                for (document in documents) {
                    val subject = Subject(
                        id = document.id,
                        nombre = document.getString("nombre") ?: "",
                        codigo = document.getString("codigo") ?: "",
                        curso_id = document.getString("curso_id") ?: "",
                        curso_nombre = document.getString("curso_nombre") ?: ""
                    )
                    subjectsList.add(subject)
                    subjectsNames.add(subject.nombre)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Error al cargar asignaturas: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun loadStudents() {
        db.collection("users")
            .whereEqualTo("rol", "Estudiante")
            .get()
            .addOnSuccessListener { documents ->
                studentsList.clear()
                studentsNames.clear()
                studentsNames.add("Seleccionar estudiante")

                for (document in documents) {
                    val user = User(
                        id = document.getString("id") ?: "",
                        nombre_completo = document.getString("nombre_completo") ?: "",
                        rol = document.getString("rol") ?: "",
                        telefono = document.getString("telefono") ?: "",
                        correo = document.getString("correo") ?: "",
                        activo = document.getBoolean("activo") ?: true
                    )

                    val student = Student(
                        grado = document.getString("grado") ?: "",
                        grupo = document.getString("grupo") ?: "",
                        user = user
                    )
                    studentsList.add(student)
                    studentsNames.add(user.nombre_completo)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Error al cargar estudiantes: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun setupAlcanceSpinner() {
        val adapter = ArrayAdapter(
            this,
            R.layout.spinner_item,
            alcanceOptions
        )
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        binding.spinnerAlcance.adapter = adapter

        binding.spinnerAlcance.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                handleAlcanceSelection(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupDatePicker() {
        binding.etFecha.setOnClickListener {
            val calendar = Calendar.getInstance()
            if (selectedDateInMillis != null) {
                calendar.timeInMillis = selectedDateInMillis!!
            }
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    val selectedCalendar = Calendar.getInstance()
                    selectedCalendar.set(selectedYear, selectedMonth, selectedDay, 0, 0, 0)
                    selectedCalendar.set(Calendar.MILLISECOND, 0)
                    selectedDateInMillis = selectedCalendar.timeInMillis

                    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    binding.etFecha.setText(dateFormat.format(selectedCalendar.time))
                    binding.tilFecha.error = null
                },
                year,
                month,
                day
            )
            datePickerDialog.show()
        }
    }

    private fun handleAlcanceSelection(position: Int) {
        // Limpiar el contenedor de spinners condicionales
        binding.containerConditionalSpinners.removeAllViews()
        conditionalSpinner = null
        conditionalLabel = null

        when (position) {
            0 -> {} // "Seleccionar alcance" - no hacer nada
            1 -> {} // "Colegio" - no necesita spinner adicional
            2 -> createConditionalSpinner("Curso", coursesNames) // "Curso"
            3 -> createConditionalSpinner("Asignatura", subjectsNames) // "Asignatura"
            4 -> createConditionalSpinner("Estudiante", studentsNames) // "Estudiante"
        }
    }

    private fun createConditionalSpinner(label: String, items: List<String>) {
        val dpToPx = { dp: Int -> (dp * resources.displayMetrics.density).toInt() }

        // Crear TextView para el label
        conditionalLabel = TextView(this).apply {
            text = label
            textSize = 16f
            setTextColor(resources.getColor(R.color.black, null))
            val paddingDp = dpToPx(16)
            setPadding(0, paddingDp, 0, dpToPx(8))
            layoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        binding.containerConditionalSpinners.addView(conditionalLabel)

        // Crear Spinner
        conditionalSpinner = Spinner(this).apply {
            id = View.generateViewId()
            layoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dpToPx(8)
            }
            minimumHeight = dpToPx(48)
            setBackgroundResource(R.drawable.spinner_border)
        }

        val adapter = ArrayAdapter(
            this,
            R.layout.spinner_item,
            items
        )
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        conditionalSpinner?.adapter = adapter
        binding.containerConditionalSpinners.addView(conditionalSpinner)
    }

    private fun loadEventData() {
        showLoading(true)

        db.collection("events")
            .document(eventId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val titulo = document.getString("titulo") ?: ""
                    val fecha = document.getTimestamp("fecha")?.toDate()?.time ?: System.currentTimeMillis()
                    val alcance = document.getString("alcance") ?: ""

                    // Establecer título
                    binding.etTitulo.setText(titulo)

                    // Establecer fecha
                    selectedDateInMillis = fecha
                    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    binding.etFecha.setText(dateFormat.format(fecha))

                    // Establecer alcance
                    val alcancePosition = alcanceOptions.indexOf(alcance)
                    if (alcancePosition >= 0) {
                        binding.spinnerAlcance.setSelection(alcancePosition)
                    }

                    // Establecer campos condicionales según el alcance
                    when (alcance) {
                        "Curso" -> {
                            val cursoNombre = document.getString("curso_nombre") ?: ""
                            val cursoPosition = coursesNames.indexOf(cursoNombre)
                            if (cursoPosition >= 0) {
                                conditionalSpinner?.setSelection(cursoPosition)
                            }
                        }
                        "Asignatura" -> {
                            val asignaturaNombre = document.getString("asignatura_nombre") ?: ""
                            val asignaturaPosition = subjectsNames.indexOf(asignaturaNombre)
                            if (asignaturaPosition >= 0) {
                                conditionalSpinner?.setSelection(asignaturaPosition)
                            }
                        }
                        "Estudiante" -> {
                            val estudianteNombre = document.getString("estudiante_nombre") ?: ""
                            val estudiantePosition = studentsNames.indexOf(estudianteNombre)
                            if (estudiantePosition >= 0) {
                                conditionalSpinner?.setSelection(estudiantePosition)
                            }
                        }
                    }
                }
                showLoading(false)
            }
            .addOnFailureListener { e ->
                showLoading(false)
                Toast.makeText(this, "Error al cargar evento: ${e.message}", Toast.LENGTH_LONG).show()
                finish()
            }
    }

    private fun setupListeners() {
        binding.btnUpdateEvent.setOnClickListener {
            updateEvent()
        }

        binding.btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun updateEvent() {
        val titulo = binding.etTitulo.text.toString().trim()
        val alcancePosition = binding.spinnerAlcance.selectedItemPosition

        if (!validateFields(titulo, alcancePosition)) {
            return
        }

        showLoading(true)

        val alcance = alcanceOptions[alcancePosition]
        val eventData = hashMapOf(
            "titulo" to titulo,
            "alcance" to alcance,
            "fecha" to Timestamp(java.util.Date(selectedDateInMillis!!))
        )

        // Agregar campos adicionales según el alcance
        when (alcancePosition) {
            2 -> { // Curso
                val cursoPosition = conditionalSpinner?.selectedItemPosition ?: 0
                if (cursoPosition == 0) {
                    showLoading(false)
                    Toast.makeText(this, "Debe seleccionar un curso", Toast.LENGTH_SHORT).show()
                    return
                }
                val selectedCourse = coursesList[cursoPosition - 1]
                eventData["curso_id"] = selectedCourse.id
                eventData["curso_nombre"] = selectedCourse.nombre
            }
            3 -> { // Asignatura
                val asignaturaPosition = conditionalSpinner?.selectedItemPosition ?: 0
                if (asignaturaPosition == 0) {
                    showLoading(false)
                    Toast.makeText(this, "Debe seleccionar una asignatura", Toast.LENGTH_SHORT).show()
                    return
                }
                val selectedSubject = subjectsList[asignaturaPosition - 1]
                eventData["asignatura_id"] = selectedSubject.id
                eventData["asignatura_nombre"] = selectedSubject.nombre
            }
            4 -> { // Estudiante
                val estudiantePosition = conditionalSpinner?.selectedItemPosition ?: 0
                if (estudiantePosition == 0) {
                    showLoading(false)
                    Toast.makeText(this, "Debe seleccionar un estudiante", Toast.LENGTH_SHORT).show()
                    return
                }
                val selectedStudent = studentsList[estudiantePosition - 1]
                eventData["estudiante_id"] = selectedStudent.user.id
                eventData["estudiante_nombre"] = selectedStudent.user.nombre_completo
            }
        }

        // Actualizar el evento en Firebase
        db.collection("events")
            .document(eventId)
            .update(eventData as Map<String, Any>)
            .addOnSuccessListener {
                showLoading(false)
                Toast.makeText(
                    this,
                    "Evento actualizado exitosamente",
                    Toast.LENGTH_LONG
                ).show()
                finish()
            }
            .addOnFailureListener { e ->
                showLoading(false)
                Toast.makeText(
                    this,
                    "Error al actualizar evento: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun validateFields(titulo: String, alcancePosition: Int): Boolean {
        var isValid = true

        if (titulo.isEmpty()) {
            binding.tilTitulo.error = "El título es requerido"
            isValid = false
        } else if (titulo.length > 160) {
            binding.tilTitulo.error = "El título no puede exceder 160 caracteres"
            isValid = false
        } else {
            binding.tilTitulo.error = null
        }

        if (selectedDateInMillis == null) {
            binding.tilFecha.error = "Debe seleccionar una fecha"
            isValid = false
        } else {
            binding.tilFecha.error = null
        }

        if (alcancePosition == 0) {
            Toast.makeText(this, "Debe seleccionar un alcance", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        return isValid
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnUpdateEvent.isEnabled = !show
        binding.btnCancel.isEnabled = !show
    }
}
