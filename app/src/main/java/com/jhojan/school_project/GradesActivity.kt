package com.jhojan.school_project

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jhojan.school_project.databinding.ActivityGradesBinding
import com.jhojan.school_project.databinding.ItemGradeBinding

data class Grade(
    val subjectId: String,
    val subjectName: String,
    val average: Double,
    val tasksCount: Int
)

class GradesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGradesBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private val grades = mutableListOf<Grade>()
    private lateinit var adapter: GradeAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGradesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupToolbar()
        setupRecyclerView()
        loadGrades()
    }

    private fun setupToolbar() {
        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        adapter = GradeAdapter(grades)
        binding.recyclerViewGrades.apply {
            layoutManager = LinearLayoutManager(this@GradesActivity)
            this.adapter = this@GradesActivity.adapter
        }
    }

    private fun loadGrades() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Cargar todas las tareas completadas del estudiante con calificación
        db.collection("tareas")
            .whereEqualTo("estudiante", currentUser.uid)
            .whereEqualTo("completada", true)
            .get()
            .addOnSuccessListener { documents ->
                // Agrupar tareas por materia y calcular promedios
                val gradesBySubject = mutableMapOf<String, MutableList<Double>>()

                for (document in documents) {
                    val nota = document.getDouble("nota") ?: 0.0
                    if (nota > 0) {
                        val materiaId = document.getString("materia") ?: ""
                        if (materiaId.isNotEmpty()) {
                            if (!gradesBySubject.containsKey(materiaId)) {
                                gradesBySubject[materiaId] = mutableListOf()
                            }
                            gradesBySubject[materiaId]?.add(nota)
                        }
                    }
                }

                // Cargar nombres de materias y crear objetos Grade
                loadSubjectNames(gradesBySubject)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al cargar calificaciones: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("GradesActivity", "Error loading grades", e)
            }
    }

    private fun loadSubjectNames(gradesBySubject: Map<String, List<Double>>) {
        if (gradesBySubject.isEmpty()) {
            Toast.makeText(this, "No hay calificaciones disponibles", Toast.LENGTH_SHORT).show()
            return
        }

        val subjectIds = gradesBySubject.keys.toList()
        var loadedCount = 0

        for (subjectId in subjectIds) {
            db.collection("subjects")
                .document(subjectId)
                .get()
                .addOnSuccessListener { document ->
                    val subjectName = document.getString("nombre") ?: "Materia"
                    val gradesList = gradesBySubject[subjectId] ?: emptyList()
                    val average = if (gradesList.isNotEmpty()) {
                        gradesList.average()
                    } else {
                        0.0
                    }

                    grades.add(
                        Grade(
                            subjectId = subjectId,
                            subjectName = subjectName,
                            average = average,
                            tasksCount = gradesList.size
                        )
                    )

                    loadedCount++
                    if (loadedCount == subjectIds.size) {
                        // Ordenar por nombre de materia
                        grades.sortBy { it.subjectName }
                        adapter.notifyDataSetChanged()
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("GradesActivity", "Error loading subject: $subjectId", e)
                    loadedCount++
                    if (loadedCount == subjectIds.size) {
                        grades.sortBy { it.subjectName }
                        adapter.notifyDataSetChanged()
                    }
                }
        }
    }
}

class GradeAdapter(
    private val grades: List<Grade>
) : RecyclerView.Adapter<GradeAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemGradeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(grade: Grade) {
            binding.tvGradeSubject.text = grade.subjectName
            binding.tvGradeValue.text = String.format("%.1f", grade.average)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemGradeBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(grades[position])
    }

    override fun getItemCount(): Int = grades.size
}
