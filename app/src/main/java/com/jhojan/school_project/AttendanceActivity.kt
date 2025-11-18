package com.jhojan.school_project

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.jhojan.school_project.CrearTareaActivity.Subject
import com.jhojan.school_project.CrearTareaActivity.User
import java.text.SimpleDateFormat
import java.util.*

class AttendanceActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var teacherHeader: TeacherHeader
    private lateinit var bottomNav: TeacherBottomNavigationView

    private val listaMaterias = mutableListOf<CrearTareaActivity.Subject>()
    private val listaEstudiantes = mutableListOf<User>()


    private lateinit var spinnerCourse: Spinner
    private lateinit var spinnerSubject: Spinner
    private lateinit var recyclerStudents: RecyclerView
    private lateinit var btnSaveAttendance: Button
    private lateinit var progressBar: ProgressBar

    private var profesorId: String = ""
    private val coursesList = mutableListOf<Course>()
    private val subjectsList = mutableListOf<Subject>()
    private val attendanceList = mutableListOf<AttendanceItem>()

    private lateinit var studentsAdapter: AttendanceAdapter

    data class Course(
        val id: String,
        val nombre: String,
        val codigo: String
    )

    data class Subject(
        val id: String,
        val nombre: String,
        val course: String
    )

    data class Student(
        val id: String,
        val nombre: String,
        val email: String
    )

    data class AttendanceItem(
        val student: Student,
        var isPresent: Boolean
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_attendance)

        // Inicializar Firestore
        db = FirebaseFirestore.getInstance()

        // Obtener ID del profesor desde SharedPreferences
        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        profesorId = prefs.getString("profesor_id", "") ?: ""

        if (profesorId.isEmpty()) {
            profesorId = "ZW6kOK1PaGVcteR4N9mzSQlcxjd2" // ID por defecto
        }

        // Inicializar vistas
        initViews()
        setupHeaderFooter()
        setupRecyclerView()

        // Cargar datos
        loadCourses()
        setupListeners()
    }

    private fun initViews() {
        teacherHeader = findViewById(R.id.teacherHeader)
        bottomNav = findViewById(R.id.bottomNav)
        spinnerCourse = findViewById(R.id.spinnerCourse)
        spinnerSubject = findViewById(R.id.spinnerSubject)
        recyclerStudents = findViewById(R.id.recyclerStudents)
        btnSaveAttendance = findViewById(R.id.btnSaveAttendance)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun setupHeaderFooter() {
        teacherHeader.loadTeacherData(profesorId)
        teacherHeader.setOnBackClickListener {
            finish()
        }

        bottomNav.setActiveItem(TeacherBottomNavigationView.NavigationItem.ASISTENCIA)
    }

    private fun setupRecyclerView() {
        studentsAdapter = AttendanceAdapter(attendanceList)
        recyclerStudents.apply {
            layoutManager = LinearLayoutManager(this@AttendanceActivity)
            adapter = studentsAdapter
        }
    }

    private fun setupListeners() {
        spinnerCourse.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (coursesList.isNotEmpty()) {
                    loadSubjects(coursesList[position].id)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        spinnerSubject.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (coursesList.isNotEmpty() && subjectsList.isNotEmpty()) {
                    val courseId = coursesList[spinnerCourse.selectedItemPosition].id
                    loadStudents(courseId)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        btnSaveAttendance.setOnClickListener {
            saveAttendance()
        }
    }

    private fun loadCourses() {
        showLoading(true)
        db.collection("courses")
            .get()
            .addOnSuccessListener { documents ->
                coursesList.clear()
                for (document in documents) {
                    val course = Course(
                        id = document.id,
                        nombre = document.getString("nombre") ?: "",
                        codigo = document.getString("codigo") ?: ""
                    )
                    coursesList.add(course)
                }
                updateCourseSpinner()
                showLoading(false)
            }
            .addOnFailureListener { e ->
                showLoading(false)
                Toast.makeText(this, "Error al cargar cursos: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadSubjects(courseId: String) {
        db.collection("subjects")
            .whereEqualTo("curso_id", courseId)
            .get()
            .addOnSuccessListener { documents ->
                subjectsList.clear()
                for (document in documents) {
                    val subject = Subject(
                        id = document.id,
                        nombre = document.getString("nombre") ?: "",
                        course = document.getString("curso_id") ?: ""
                    )
                    subjectsList.add(subject)
                }
                updateSubjectSpinner()  // 🔥 CORREGIDO
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al cargar materias: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun loadStudents(courseId: String) {
        showLoading(true)
        db.collection("users")
            .get()
            .addOnSuccessListener { documents ->

                listaEstudiantes.clear()
                attendanceList.clear()

                for (document in documents) {
                    val id = document.id
                    val nombre = document.getString("nombre")
                        ?: document.getString("name")
                        ?: document.getString("displayName")
                        ?: "Usuario sin nombre"

                    val rol = document.getString("rol") ?: document.getString("role")
                    if (rol == null || rol == "estudiante" || rol == "student") {

                        val student = Student(id, nombre, "")

                        listaEstudiantes.add(User(id, nombre))
                        attendanceList.add(AttendanceItem(student, true))
                    }
                }

                studentsAdapter.notifyDataSetChanged()
                showLoading(false)
            }
            .addOnFailureListener { e ->
                showLoading(false)
                Toast.makeText(this, "Error al cargar estudiantes: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun saveAttendance() {
        if (coursesList.isEmpty() || subjectsList.isEmpty() || attendanceList.isEmpty()) {
            Toast.makeText(this, "Seleccione curso, materia y estudiantes", Toast.LENGTH_SHORT).show()
            return
        }

        showLoading(true)

        val courseId = coursesList[spinnerCourse.selectedItemPosition].id
        val subjectId = subjectsList[spinnerSubject.selectedItemPosition].id
        val currentDate = Timestamp.now()

        // Obtener solo fecha sin hora
        val calendar = Calendar.getInstance()
        calendar.time = currentDate.toDate()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = Timestamp(calendar.time)

        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val startOfNextDay = Timestamp(calendar.time)

        var successCount = 0
        var errorCount = 0
        val totalStudents = attendanceList.size

        for (attendanceItem in attendanceList) {

            // 🔥 ESTA ES LA VALIDACIÓN CORRECTA
            db.collection("asistencias")
                .whereEqualTo("estudiante", attendanceItem.student.id)
                .whereEqualTo("curso", courseId)
                .whereEqualTo("materia", subjectId)
                .whereGreaterThanOrEqualTo("fecha", startOfDay)
                .whereLessThan("fecha", startOfNextDay)
                .get()
                .addOnSuccessListener { documents ->

                    if (documents.isEmpty) {

                        // 🔥 CAMPOS QUE TIENES EN FIRESTORE
                        val attendanceData = hashMapOf(
                            "attendance" to attendanceItem.isPresent,
                            "curso" to courseId,
                            "fecha" to currentDate,
                            "estudiante" to attendanceItem.student.id,
                            "materia" to subjectId,
                            "profesor" to profesorId
                        )

                        db.collection("asistencias")
                            .add(attendanceData)
                            .addOnSuccessListener {
                                successCount++
                                checkCompletion(successCount, errorCount, totalStudents)
                            }
                            .addOnFailureListener {
                                errorCount++
                                checkCompletion(successCount, errorCount, totalStudents)
                            }

                    } else {
                        // Ya existía — no duplicar
                        errorCount++
                        checkCompletion(successCount, errorCount, totalStudents)
                    }
                }
                .addOnFailureListener {
                    errorCount++
                    checkCompletion(successCount, errorCount, totalStudents)
                }
        }
    }


    private fun checkCompletion(successCount: Int, errorCount: Int, totalStudents: Int) {
        if (successCount + errorCount == totalStudents) {
            showLoading(false)
            if (successCount > 0) {
                Toast.makeText(
                    this,
                    "Asistencia guardada: $successCount registrados, $errorCount omitidos (ya existían)",
                    Toast.LENGTH_LONG
                ).show()
                // Limpiar selección
                for (item in attendanceList) {
                    item.isPresent = false
                }
                studentsAdapter.notifyDataSetChanged()
            } else {
                Toast.makeText(
                    this,
                    "No se guardó ninguna asistencia (ya existían registros para hoy)",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun updateCourseSpinner() {
        val nombres = coursesList.map { "${it.nombre} (${it.codigo})" }

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, nombres)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCourse.adapter = adapter
    }


    private fun updateSubjectSpinner() {
        val nombres = subjectsList.map { it.nombre }

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, nombres)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSubject.adapter = adapter
    }


    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        btnSaveAttendance.isEnabled = !show
    }
}