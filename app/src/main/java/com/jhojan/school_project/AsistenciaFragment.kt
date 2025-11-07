package com.jhojan.school_project

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jhojan.school_project.databinding.FragmentAsistenciaBinding
import java.text.SimpleDateFormat
import java.util.*

class AsistenciaFragment : Fragment() {

    private var _binding: FragmentAsistenciaBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val students = mutableListOf<EstudianteAsistencia>()
    private lateinit var adapter: StudentAttendanceAdapter
    private var claseId: String = ""
    private var claseName: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAsistenciaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Obtener argumentos
        claseId = arguments?.getString("claseId") ?: ""
        claseName = arguments?.getString("claseName") ?: ""

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
        binding.rvStudents.layoutManager = LinearLayoutManager(requireContext())
        binding.rvStudents.adapter = adapter
    }

    private fun loadStudents() {
        showLoading(true)

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
                                    status = "presente"
                                )
                                students.add(student)
                            }

                            loadedCount++
                            if (loadedCount == studentIds.size) {
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
                Toast.makeText(requireContext(), "Error al cargar estudiantes", Toast.LENGTH_SHORT).show()
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

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dateString = sdf.format(Date())

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

        val sessionId = "${claseId}_$dateString"
        db.collection("asistencias")
            .document(sessionId)
            .set(attendanceData)
            .addOnSuccessListener {
                showLoading(false)
                Toast.makeText(requireContext(), "Asistencia guardada exitosamente", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
            .addOnFailureListener {
                showLoading(false)
                Toast.makeText(requireContext(), "Error al guardar asistencia", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnSaveAttendance.isEnabled = !show
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
