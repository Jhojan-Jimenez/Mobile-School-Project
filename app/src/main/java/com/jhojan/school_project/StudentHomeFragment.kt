package com.jhojan.school_project

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jhojan.school_project.databinding.FragmentStudentHomeBinding
import java.text.SimpleDateFormat
import java.util.Locale

data class Tarea(
    val id: String = "",
    val titulo: String = "",
    val descripcion: String = "",
    val estudiante: String = "",
    val fecha_entrega: Long = 0L,
    val materia: String = "",
    val nota: Double = 0.0,
    val profesor: String = "",
    val completada: Boolean = false
)

class StudentHomeFragment : Fragment() {

    private var _binding: FragmentStudentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStudentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        loadUserData()
        loadPendingTasks()
        loadGradeAverage()
        setupUI()
        setupClickListeners()
    }

    private fun loadUserData() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            db.collection("users")
                .document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val nombreCompleto = document.getString("nombre_completo") ?: "Estudiante"
                        val rol = document.getString("rol") ?: "Estudiante"

                        binding.tvStudentName.text = nombreCompleto
                        binding.tvRole.text = rol
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Error al cargar datos: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun loadPendingTasks() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            db.collection("tareas")
                .whereEqualTo("estudiante", currentUser.uid)
                .whereEqualTo("completada", false)
                .get()
                .addOnSuccessListener { documents ->
                    val tareas = mutableListOf<Tarea>()
                    for (document in documents) {
                        val tarea = Tarea(
                            id = document.id,
                            titulo = document.getString("titulo") ?: "",
                            descripcion = document.getString("descripcion") ?: "",
                            estudiante = document.getString("estudiante") ?: "",
                            fecha_entrega = document.getTimestamp("fecha_entrega")?.toDate()?.time ?: 0L,
                            materia = document.getString("materia") ?: "",
                            nota = document.getDouble("nota") ?: 0.0,
                            profesor = document.getString("profesor") ?: "",
                            completada = document.getBoolean("completada") ?: false
                        )
                        tareas.add(tarea)
                    }

                    // Ordenar por fecha de entrega en el cliente y tomar solo las 2 primeras
                    val sortedTareas = tareas.sortedBy { it.fecha_entrega }.take(2)
                    updateTasksUI(sortedTareas)
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Error al cargar tareas: ${e.message}", Toast.LENGTH_SHORT).show()
                    // Mostrar en Logcat
                    Log.e("FirestoreError", "Error al cargar tareas", e)
                }
        }
    }

    private fun updateTasksUI(tareas: List<Tarea>) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        // Actualizar las cards de tareas con datos reales
        if (tareas.isNotEmpty()) {
            // Primera tarea
            if (tareas.size > 0) {
                binding.tvTask1Title.text = tareas[0].titulo
                binding.tvTask1Date.text = dateFormat.format(tareas[0].fecha_entrega)

                loadSubjectName(tareas[0].materia) { subjectName ->
                    binding.tvTask1Subject.text = subjectName
                }
            }

            // Segunda tarea
            if (tareas.size > 1) {
                binding.tvTask2Title.text = tareas[1].titulo
                binding.tvTask2Date.text = dateFormat.format(tareas[1].fecha_entrega)

                loadSubjectName(tareas[1].materia) { subjectName ->
                    binding.tvTask2Subject.text = subjectName
                }
            }
        }
    }

    private fun loadSubjectName(subjectId: String, callback: (String) -> Unit) {
        db.collection("subjects")
            .document(subjectId)
            .get()
            .addOnSuccessListener { document ->
                val subjectName = document.getString("nombre") ?: "Materia"
                callback(subjectName)
            }
            .addOnFailureListener {
                callback("Materia")
            }
    }

    private fun loadGradeAverage() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            db.collection("tareas")
                .whereEqualTo("estudiante", currentUser.uid)
                .whereEqualTo("completada", true)
                .get()
                .addOnSuccessListener { documents ->
                    var totalGrade = 0.0
                    var count = 0

                    for (document in documents) {
                        val nota = document.getDouble("nota") ?: 0.0
                        if (nota > 0) {
                            totalGrade += nota
                            count++
                        }
                    }

                    val average = if (count > 0) totalGrade / count else 0.0
                    binding.tvAverageGrade.text = String.format("%.1f", average)
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Error al calcular promedio: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun setupUI() {
        Glide.with(this)
            .load("https://lh3.googleusercontent.com/aida-public/AB6AXuAnP2WWoz9Cc6kaOMAkhsMy0xrMLZ4qrvd790pNEAMpwe0BK9eEVmj_5Gv4xe0PudRpyKDTfRHDUpH5_KJtl7-CrepjoNygXV5giJh3J7PYxXL64Tbu-jrmef7uFUWqw3uF6K8_Vdu3T81SuUpb0xWQ-bFIzb0LGgcHs7kGEm0jcsdi-xzq__8ciXs68RxTCoNtWkKf28fBaSMeSN0uhESIlEiCDyRd4fkrodGP3jBYRbN1OUiaTds-rqxAaCL11ORbU4uIfbMfyY4")
            .placeholder(android.R.color.darker_gray)
            .into(binding.imgAvatar)

        Glide.with(this)
            .load("https://lh3.googleusercontent.com/aida-public/AB6AXuAKn05uR2YOZRa3vdVty-Y9PIASuyVr-XEcMrGz5mocSER54XVboJHkizlqrsJcmwDyKEDQ6lzZ8-R_93bpI335s3ao5qFj9SI5hkI-toF2J2f2PicAeSLiNkUf8gLRs297VsTvh2lDMQFMUrk4wIG1axll2O-rq36xu2_PVG15QjhcpJhBlCVpIVo4EaHwRHmzPjp5NLKm8YYJGHPDTQBjjr9-lhcEE7XWBZMBaTML2T28ju0RT9DGaSWHHXGbhhNB3USUf78ijT8")
            .placeholder(android.R.color.darker_gray)
            .into(binding.imgCard1)

        Glide.with(this)
            .load("https://lh3.googleusercontent.com/aida-public/AB6AXuBJNku3r68W-MwS-UFV21PSgaSHwq9X6kv45mMR0XD8vw5sahpDBvuHFvZIkN6qev2JHc1t3qpLkidmSTh_bJQiC_UbFj9C_MPD01vyDB3GgBDCgtz0a5q1AzotgQCyObvrFnqMnbIFNVsN4ISNlZnlgSeDRXM0M_be0MZ78K999Y8JG3b9UuEGas4l1r8cHfAZnbbwvTCYO1HApvT1w8MtRobDKJ7QLCGaL-j2VPsnktq63ofXIjQf1aaT3wmmEqkOrjL7ZTIQYC0")
            .placeholder(android.R.color.darker_gray)
            .into(binding.imgCard2)

        Glide.with(this)
            .load("https://lh3.googleusercontent.com/aida-public/AB6AXuDYc7_Uiz98j5i7uIXe0zBhxLRzKFRzVeHZnBBukLFjX0WLv9zRD6U3Tpv2Xu8xNQ22u40VOHGYDETfeQlSf3oklz97alJVY_trUpMeQEQsUfXtSaQxaUalofv8cOBBYqP1JExhH_NRiL90ZFbPQhNRXwZkWVJjig0Oto4_sruPHMAmPp9kjEIX6zmSrp8EnGaZ6RblY38NGWRjc4AlKljbq8k_bBrPLVhfP9EMz74KIEQdDJsNEMC566KPcWkYYwOoOO2lu-BI74k")
            .placeholder(android.R.color.darker_gray)
            .into(binding.imgCard3)
    }

    private fun setupClickListeners() {
        // Card 1: Calendario - Ver fechas importantes
        binding.cardCalendar.setOnClickListener {
            val intent = Intent(requireContext(), CalendarActivity::class.java)
            startActivity(intent)
        }

        // Card 2: Tareas - Consultar tareas
        binding.cardTasks.setOnClickListener {
            val intent = Intent(requireContext(), TasksActivity::class.java)
            startActivity(intent)
        }

        // Card 3: Notas - Ver notas generales
        binding.cardGrades.setOnClickListener {
            val intent = Intent(requireContext(), GradesActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
