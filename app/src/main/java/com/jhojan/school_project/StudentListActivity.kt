package com.jhojan.school_project

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.jhojan.school_project.databinding.ActivityStudentListBinding

class StudentListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStudentListBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: StudentAdapter
    private val students = mutableListOf<Student>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()

        setupRecyclerView()
        setupListeners()
        loadStudents()
    }

    private fun setupRecyclerView() {
        adapter = StudentAdapter(students) { student ->
            // Handle edit click
            Toast.makeText(this, "Editar: ${student.user.nombre}", Toast.LENGTH_SHORT).show()
        }

        binding.recyclerViewStudents.apply {
            layoutManager = LinearLayoutManager(this@StudentListActivity)
            adapter = this@StudentListActivity.adapter
        }
    }

    private fun setupListeners() {
        binding.fabAddStudent.setOnClickListener {
            val intent = Intent(this, CreateUserActivity::class.java)
            startActivity(intent)
        }

        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun loadStudents() {
        showLoading(true)

        db.collection("users")
            .whereEqualTo("rol", "Estudiante")
            .get()
            .addOnSuccessListener { documents ->
                students.clear()
                for (document in documents) {
                    val user = User(
                        id = document.getString("id") ?: "",
                        nombre = document.getString("nombre") ?: "",
                        apellido = document.getString("apellido") ?: "",
                        rol = document.getString("rol") ?: "",
                        telefono = document.getString("telefono") ?: "",
                        Direccion = document.getString("Direccion") ?: "",
                        correo = document.getString("correo") ?: ""
                    )

                    val student = Student(
                        grado = document.getString("grado") ?: "",
                        grupo = document.getString("grupo") ?: "",
                        user = user
                    )
                    students.add(student)
                }
                adapter.notifyDataSetChanged()
                showLoading(false)

                if (students.isEmpty()) {
                    Toast.makeText(this, "No hay estudiantes registrados", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                showLoading(false)
                Toast.makeText(this, "Error al cargar estudiantes: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.recyclerViewStudents.visibility = if (show) View.GONE else View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        loadStudents()
    }
}

