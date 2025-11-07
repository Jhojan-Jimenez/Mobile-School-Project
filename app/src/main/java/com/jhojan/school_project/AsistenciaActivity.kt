package com.jhojan.school_project

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jhojan.school_project.databinding.ActivityAsistenciaBinding
import java.text.SimpleDateFormat
import java.util.*

class AsistenciaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAsistenciaBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val students = mutableListOf<EstudianteAsistencia>()
    private lateinit var adapter: StudentAttendanceAdapter
    private lateinit var claseId: String
    private lateinit var claseName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAsistenciaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtener datos de la clase
        claseId = intent.getStringExtra("claseId") ?: ""
        claseName = intent.getStringExtra("claseName") ?: ""

        setupUI()
        setupRecyclerView()
        loadStudents()
        setupSaveButton()
    }

    private fun setupUI() {
        binding.tvClassName.text = claseName
        val sdf = SimpleDateFormat("EEEE, dd 'de' MMMM yyyy", Locale("es", "ES"))
        binding.tvDate.text = sdf.format(Date())
    }

    private fun setupRecyclerView() {
        adapter = StudentAttendanceAdapter(students)
        binding.rvStudents.layoutManager = LinearLayoutManager(this)
        binding.rvStudents.adapter = adapter
    }

    private fun loadStudents() {
        showLoading(true)

        // Primero, obtener la lista de estudiantes de la clase desde la subcolección
        db.collection("clases")
            .document(claseId)
            .collection("estudiantes")
            .get()
            .addOnSuccessListener { documents ->
                students.clear()
                val studentIds = documents.map { it.id }

                if (studentIds.isEmpty()) {
                    showLoading(false)
                    binding.tvEmptyState.visibility = View.VISIBLE
                    return@addOnSuccessListener
                }

                // Ahora obtener los nombres desde la colección users
                var loadedCount = 0
                for (studentId in studentIds) {
                    db.collection("users")
                        .document(studentId)
                        .get()
                        .addOnSuccessListener { userDoc ->
                            if (userDoc.exists()) {
                                val nombre = userDoc.getString("nombre") ?: ""
                                val apellido = userDoc.getString("apellido") ?: ""
                                val fullName = "$nombre $apellido".trim()

                                val student = EstudianteAsistencia(
                                    studentId = studentId,
                                    studentName = fullName,
                                    status = "presente" // Por defecto presente
                                )
                                students.add(student)
                            }

                            loadedCount++
                            if (loadedCount == studentIds.size) {
                                // Todos los estudiantes cargados
                                adapter.notifyDataSetChanged()
                                showLoading(false)
                                binding.tvEmptyState.visibility = View.GONE
                            }
                        }
                        .addOnFailureListener {
                            loadedCount++
                            if (loadedCount == studentIds.size) {
                                adapter.notifyDataSetChanged()
                                showLoading(false)
                                if (students.isEmpty()) {
                                    binding.tvEmptyState.visibility = View.VISIBLE
                                }
                            }
                        }
                }
            }
            .addOnFailureListener {
                showLoading(false)
                Toast.makeText(this, "Error al cargar estudiantes", Toast.LENGTH_SHORT).show()
                binding.tvEmptyState.visibility = View.VISIBLE
            }
    }

    private fun setupSaveButton() {
        binding.btnSaveAttendance.setOnClickListener {
            saveAttendance()
        }
    }

    private fun saveAttendance() {
        val currentUser = auth.currentUser ?: return
        showLoading(true)

        // Crear fecha en formato yyyy-MM-dd
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dateString = sdf.format(Date())

        // Preparar datos de asistencia
        val attendanceData = hashMapOf(
            "claseId" to claseId,
            "date" to dateString,
            "timestamp" to System.currentTimeMillis(),
            "teacherId" to currentUser.uid,
            "students" to adapter.getAttendanceData().map { student ->
                hashMapOf(
                    "studentId" to student.studentId,
                    "studentName" to student.studentName,
                    "status" to student.status
                )
            }
        )

        // Guardar en Firestore con ID personalizado (claseId_date)
        val sessionId = "${claseId}_$dateString"
        db.collection("asistencias")
            .document(sessionId)
            .set(attendanceData)
            .addOnSuccessListener {
                showLoading(false)
                Toast.makeText(this, "Asistencia guardada exitosamente", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                showLoading(false)
                Toast.makeText(this, "Error al guardar asistencia", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnSaveAttendance.isEnabled = !show
    }
}
